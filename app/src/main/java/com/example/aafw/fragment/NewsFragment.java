package com.example.aafw.fragment;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.aafw.R;
import com.example.aafw.adapter.NewsAdapter;
import com.example.aafw.entity.News;
import com.example.aafw.listener.OnItemClickListener;
import com.example.aafw.util.Constants;
import com.example.aafw.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NewsFragment extends Fragment {
    private static final String TAG = "aafw";

    private RequestQueue requestQueue;

    // 로딩 레이아웃, 데이터 없을때 표시할 레이아웃
    private LinearLayout layLoading, layNoData;

    private RecyclerView recyclerView;
    private NewsAdapter adapter;
    private ArrayList<News> items;

    private EditText editKeyword;

    private InputMethodManager imm;                     // 키보드를 숨기기 위해 필요함

    private static final int ITEM_PAGE_SIZE =  100;     // 최대 100

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        // 로딩 레이아웃
        this.layLoading = view.findViewById(R.id.layLoading);
        ((ProgressBar) view.findViewById(R.id.progressBar)).setIndeterminateTintList(ColorStateList.valueOf(Color.WHITE));

        // 데이터 없을때 표시할 레이아웃
        this.layNoData = view.findViewById(R.id.layNoData);

        this.recyclerView = view.findViewById(R.id.recyclerView);
        this.recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        this.editKeyword = view.findViewById(R.id.editKeyword);
        this.editKeyword.setImeOptions(EditorInfo.IME_ACTION_DONE);
        this.editKeyword.setHint("뉴스 검색어");

        // 키보드를 숨기기 위해 필요함
        this.imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        view.findViewById(R.id.btnSearch).setOnClickListener(v -> {
            // 검색
            String keyword = this.editKeyword.getText().toString();
            if (TextUtils.isEmpty(keyword)) {
                Toast.makeText(getContext(), R.string.msg_keyword_check_empty, Toast.LENGTH_SHORT).show();
                this.editKeyword.requestFocus();
                return;
            }

            // 키보드 숨기기
            this.imm.hideSoftInputFromWindow(this.editKeyword.getWindowToken(), 0);

            // 로딩 레이아웃 보임
            this.layLoading.setVisibility(View.VISIBLE);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                // 네이버 뉴스 api 요청
                goNewsApi(keyword);
            }, Constants.LoadingDelay.SHORT);
        });

        return view;
    }

    /* 네이버 뉴스 api 요청 */
    private void goNewsApi(String keyword) {
        if (this.requestQueue == null) {
            this.requestQueue = Volley.newRequestQueue(getContext());
        }

        try {
            String url = Constants.NaverNewsApi.ADDRESS;
            url += "?query=" + URLEncoder.encode(keyword, "UTF-8");     // 필수 (검색을 원하는 문자열로서 UTF-8로 인코딩한다.)
            url += "&display=" + ITEM_PAGE_SIZE;                        // 선택 (검색 결과 출력 건수 지정) 10(기본값), 100(최대)
            url += "&start=1";                                          // 선택 (검색 시작 위치로 최대 1000까지 가능) 1(기본값), 1000(최대)
            url += "&sort=date";                                        // 선택 (정렬 옵션: sim (유사도순), date (날짜순)) sim, date(기본값)

            Log.d(TAG, "url:" + url);

            JsonObjectRequest request = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null,
                    response -> {
                        // 성공
                        Log.d(TAG, "result:" + response.toString());

                        this.items = new ArrayList<>();

                        try {
                            JSONArray jsonArray = response.getJSONArray("items");
                            Log.d(TAG, "count:" + jsonArray.length());
                            if (jsonArray.length() > 0) {
                                this.layNoData.setVisibility(View.GONE);

                                // 뉴스 목록 구성
                                for (int i=0; i<jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    News news = new News(object.getString("title"), object.getString("link"), object.getString("pubDate"));
                                    this.items.add(news);
                                }
                            } else {
                                // 뉴스 기사 없음
                                this.layNoData.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException ignored) {}

                        this.adapter = new NewsAdapter(mItemClickListener, this.items);
                        this.recyclerView.setAdapter(this.adapter);

                        this.layLoading.setVisibility(View.GONE);
                    },
                    error -> {
                        // 오류
                        Log.d(TAG, "error:" + error.toString());
                        this.layLoading.setVisibility(View.GONE);
                        Toast.makeText(getContext(), R.string.msg_error, Toast.LENGTH_SHORT).show();
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String>  params = new HashMap<>();
                    params.put("X-Naver-Client-Id", Constants.NaverNewsApi.CLIENT_ID);
                    params.put("X-Naver-Client-Secret", Constants.NaverNewsApi.CLIENT_SECRET);

                    return params;
                }
            };

            request.setShouldCache(false);      // 이전 결과가 있어도 새로 요청 (cache 사용 안함)
            this.requestQueue.add(request);
        } catch (Exception e) {
            // 오류
            Log.d(TAG, "error:" + e.toString());
            this.layLoading.setVisibility(View.GONE);
            Toast.makeText(getContext(), R.string.msg_error, Toast.LENGTH_SHORT).show();
        }
    }

    private final OnItemClickListener mItemClickListener = (view, position) -> {
        // 선택
        if (!TextUtils.isEmpty(this.items.get(position).link)) {
            // 웹사이트로 연결
            Utils.linkURL(getContext(), this.items.get(position).link);
        }
    };

}
