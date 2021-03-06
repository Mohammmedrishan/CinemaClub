package com.create.sidhu.movbox.activities;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.create.sidhu.movbox.Interfaces.CallbackDelegate;
import com.create.sidhu.movbox.Interfaces.SqlDelegate;
import com.create.sidhu.movbox.R;
import com.create.sidhu.movbox.adapters.ReviewAdapter;
import com.create.sidhu.movbox.helpers.EmailHelper;
import com.create.sidhu.movbox.helpers.ModelHelper;
import com.create.sidhu.movbox.helpers.SqlHelper;
import com.create.sidhu.movbox.helpers.StringHelper;
import com.create.sidhu.movbox.models.MovieModel;
import com.create.sidhu.movbox.models.ReviewModel;
import com.create.sidhu.movbox.models.UserModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReviewsActivity extends AppCompatActivity implements SqlDelegate {

    RequestOptions requestOptions;
    TextView tvTitle, tvSubtitle, tvTabTop, tvTabFollowing, tvTabAll;
    CircleImageView imgUser;
    ImageView imgClose;
    EditText etReviewText;
    RecyclerView recyclerView;
    Button btnReviewSubmit;
    LinearLayout llPlaceholder, llContainerTitle, llContainerWriteReview, llContainerTab, llreviewContainer;
    Typeface tfSemibold;
    Typeface tfRegular;
    Bundle bundle;
    private String userId, movieId, type;
    private ArrayList<ReviewModel> reviewModels, reviewModelsTop, reviewModelsFollowing;
    private HashMap<String, Integer> globalReplyMask;
    String currentTab;

    public static CallbackDelegate currentFragment;
    private int counter = 0;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.img_Close:{
                    finish();
                    break;
                }
                case R.id.img_User:{
                    getUserDetails(userId);
                    break;
                }
                case R.id.textView_TabAll:{
                    toggleTab(R.id.textView_TabAll);
                    attachAdapter(recyclerView, reviewModels);
                    break;
                }
                case R.id.textView_TabFollowing:{
                    toggleTab(R.id.textView_TabFollowing);
                    attachAdapter(recyclerView, reviewModelsFollowing);
                    break;
                }
                case R.id.textView_TabTop:{
                    toggleTab(R.id.textView_TabTop);
                    attachAdapter(recyclerView, reviewModelsTop);
                    break;
                }
                case R.id.btn_ReviewSubmit:{
                    if(!etReviewText.getText().toString().isEmpty()){
                        btnReviewSubmit.setEnabled(false);
                        submitReview("original", "", etReviewText.getText().toString());
                    }
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_reviews);
            Toolbar toolbar = findViewById(R.id.toolbar);
            ImageView imgTitle = (ImageView) toolbar.findViewById(R.id.imgToolbarImage);
            imgTitle.setVisibility(View.GONE);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Reviews");
            bundle = getIntent().getBundleExtra("bundle");
            type = bundle.getString("type");
            requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.ic_placeholder);
            requestOptions.error(R.drawable.ic_placeholder);
            tvTitle = (TextView) findViewById(R.id.textView_Title);
            tvSubtitle = (TextView) findViewById(R.id.textView_Subtitle);
            tvTabTop = (TextView) findViewById(R.id.textView_TabTop);
            tvTabFollowing = (TextView) findViewById(R.id.textView_TabFollowing);
            tvTabAll = (TextView) findViewById(R.id.textView_TabAll);
            imgUser = (CircleImageView) findViewById(R.id.img_User);
            imgClose = (ImageView) findViewById(R.id.img_Close);
            etReviewText = (EditText) findViewById(R.id.editText_ReviewText);
            btnReviewSubmit = (Button) findViewById(R.id.btn_ReviewSubmit);
            recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
            llPlaceholder = (LinearLayout) findViewById(R.id.containerPlaceholder);
            llContainerTitle = (LinearLayout) findViewById(R.id.containerTitle);
            llContainerWriteReview = (LinearLayout) findViewById(R.id.containerWriteReview);
            llContainerTab = (LinearLayout) findViewById(R.id.containerTabs);
            llreviewContainer = findViewById(R.id.reviewcontainer);
            tfSemibold = Typeface.createFromAsset(this.getAssets(), "fonts/MyriadPro-Semibold.otf");
            tfRegular = Typeface.createFromAsset(this.getAssets(), "fonts/myriadpro.otf");
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            imgClose.setOnClickListener(onClickListener);
            if (type.equals("movie")) {
                movieId = bundle.getString("movie_id");
                userId = bundle.getString("user_id");
                fetchReview();
                tvTitle.setText(bundle.getString("movie_name") + " (" + StringHelper.toTitleCase(bundle.getString("movie_language")) + ") (" + bundle.getString("movie_dimension") + ")");
                tvSubtitle.setText(bundle.getString("movie_genre"));
                Glide.with(ReviewsActivity.this)
                        .setDefaultRequestOptions(requestOptions)
                        .asBitmap()
                        .load(getString(R.string.master_url) + getString(R.string.profile_image_url) + userId + ".jpg")
                        .into(imgUser);
                imgUser.setOnClickListener(onClickListener);
                tvTabFollowing.setOnClickListener(onClickListener);
                tvTabAll.setOnClickListener(onClickListener);
                tvTabTop.setOnClickListener(onClickListener);
                btnReviewSubmit.setOnClickListener(onClickListener);
            } else if (type.equals("user")) {
                userId = bundle.getString("user_id");
                fetchReview();
                llContainerTitle.setVisibility(View.GONE);
                llContainerWriteReview.setVisibility(View.GONE);
                llContainerTab.setVisibility(View.GONE);
            }
        }catch (Exception e){
            EmailHelper emailHelper = new EmailHelper(ReviewsActivity.this, EmailHelper.TECH_SUPPORT, "Error: ReviewsActivity", e.getMessage() + "\n" + StringHelper.convertStackTrace(e));
            emailHelper.sendEmail();
        }

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(currentFragment != null) {
            HashMap<String, String> extras = new HashMap<>();
            extras.put("counter", "" + counter);
            extras.put("m_id", bundle.getString("movie_id"));
            currentFragment.onResultReceived("review", counter > 0, extras);
        }
    }

    @Override
    public void onResponse(SqlHelper sqlHelper) {
        try {
            if(sqlHelper.getActionString().equals("reviews")) {
                JSONArray jsonArray = sqlHelper.getJSONResponse().getJSONArray("user_data");
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                String response = jsonObject.getString("response");
                if (response.equals(getString(R.string.response_success))) {
                    initRecyclerView(jsonArray);
                } else if (response.equals(getString(R.string.response_unsuccessful))) {
                    recyclerView.setVisibility(View.GONE);
                    llPlaceholder.setVisibility(View.VISIBLE);
                } else if (response.equals(getString(R.string.unexpected))) {
                    throw new Exception();
                }
            }else if(sqlHelper.getActionString().equals("get_user")){
                JSONObject jsonObject = sqlHelper.getJSONResponse().getJSONObject("user_data");
                String response = jsonObject.getString("response");
                if(response.equals(getString(R.string.response_success))){
                    ModelHelper modelHelper = new ModelHelper(ReviewsActivity.this);
                    UserModel userModel = modelHelper.buildUserModel(jsonObject);
                    Bundle bundle = modelHelper.buildUserModelBundle(userModel, "ProfileFragment");
                    bundle.putString("return_path", "ProfileFragment");
                    Intent intent = new Intent(ReviewsActivity.this, MainActivity.class);
                    intent.putExtra("bundle", bundle);
                    startActivity(intent);
                }else if(response.equals(getString(R.string.response_unsuccessful))){
                    Toast.makeText(ReviewsActivity.this, getString(R.string.response_unsuccessful), Toast.LENGTH_SHORT).show();
                }else if(response.equals(getString(R.string.unexpected))){
                    Toast.makeText(ReviewsActivity.this, getString(R.string.unexpected), Toast.LENGTH_SHORT).show();
                }
            }else if(sqlHelper.getActionString().equals("get_movie")){
                JSONObject jsonObject = sqlHelper.getJSONResponse().getJSONObject("movie_data");
                String response = jsonObject.getString("response");
                if(response.equals(getString(R.string.response_success))){
                    ModelHelper modelHelper = new ModelHelper(ReviewsActivity.this);
                    MovieModel movieModel = modelHelper.buildMovieModel(jsonObject);
                    Bundle bundle = modelHelper.buildMovieModelBundle(movieModel, "ProfileFragment");
                    bundle.putString("return_path", "ProfileFragment");
                    Intent intent = new Intent(ReviewsActivity.this, MainActivity.class);
                    intent.putExtra("bundle", bundle);
                    startActivity(intent);
                }else if(response.equals(getString(R.string.response_unsuccessful)) || response.equals(getString(R.string.unexpected))){
                    Toast.makeText(ReviewsActivity.this, getString(R.string.unexpected), Toast.LENGTH_SHORT).show();
                }
            }else if(sqlHelper.getActionString().equals("add_review")){
                JSONObject jsonObject = sqlHelper.getJSONResponse().getJSONObject("data");
                String response = jsonObject.getString("response");
                btnReviewSubmit.setEnabled(true);
                if(response.equals(getString(R.string.response_success))){
                    etReviewText.setText("");
                    counter++;
                    MainActivity.currentUserModel.setTotalReviews(MainActivity.currentUserModel.getTotalReviews() + 1);
                    fetchReview();
                    new ModelHelper(ReviewsActivity.this).addToUpdatesModel(movieId, jsonObject.getString("r_id"), "review");
                }else if(response.equals(getString(R.string.response_unsuccessful))){
                    Toast.makeText(ReviewsActivity.this, getString(R.string.unexpected), Toast.LENGTH_SHORT).show();
                }else if(response.equals(getString(R.string.unexpected))){
                    Toast.makeText(ReviewsActivity.this, getString(R.string.unexpected), Toast.LENGTH_SHORT).show();
                }
            }else if(sqlHelper.getActionString().equals("update_like")){
                String response = sqlHelper.getJSONResponse().getJSONObject("data").getString("response");
                int position = Integer.parseInt(sqlHelper.getExtras().get("position"));
                ArrayList<ReviewModel> model = new ArrayList<>();
                if(response.equals(getString(R.string.response_success))){
                    if(currentTab.equals("top"))
                        model = reviewModelsTop;
                    else if(currentTab.equals("following"))
                        model = reviewModelsFollowing;
                    else if(currentTab.equals("all"))
                        model = reviewModels;
                    if(model.get(position).getLiked())
                        model.get(position).setLikes(model.get(position).getLikes() + 1);
                    else
                        model.get(position).setLikes(model.get(position).getLikes() - 1);
                }else if(response.equals(getString(R.string.response_unsuccessful)) || response.equals(getString(R.string.unexpected))){
                    model.get(position).setLiked(!model.get(position).getLiked());
                    Toast.makeText(ReviewsActivity.this, getString(R.string.unexpected), Toast.LENGTH_SHORT).show();
                }
                recyclerView.getAdapter().notifyItemChanged(position);
            }else if(sqlHelper.getActionString().startsWith("update_review")){
                String response = sqlHelper.getJSONResponse().getJSONObject("review_data").getString("response");
                if(response.equals(getString(R.string.response_success))){
                    if(sqlHelper.getActionString().contains("remove"))
                        fetchReview();
                }else if(response.equals(getString(R.string.response_unsuccessful)) || response.equals(getString(R.string.unexpected))){
                    Toast.makeText(this, getString(R.string.unexpected), Toast.LENGTH_SHORT).show();
                    fetchReview();
                }
            }
        }catch (Exception e){
            EmailHelper emailHelper = new EmailHelper(ReviewsActivity.this, EmailHelper.TECH_SUPPORT, "Error: ReviewsActivity", e.getMessage() + "\n" + StringHelper.convertStackTrace(e));
            emailHelper.sendEmail();
            Toast.makeText(ReviewsActivity.this, getString(R.string.unexpected), Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
    }

    private void toggleTab(int id){
        currentTab = (id == R.id.textView_TabTop ? "top" : id == R.id.textView_TabFollowing ? "following" : "all");
        tvTabTop.setTextColor(id == R.id.textView_TabTop ? getResources().getColor(R.color.colorTextPrimary) : getResources().getColor(R.color.colorTextSecondary));
        tvTabAll.setTextColor(id == R.id.textView_TabAll ? getResources().getColor(R.color.colorTextPrimary) : getResources().getColor(R.color.colorTextSecondary));
        tvTabFollowing.setTextColor(id == R.id.textView_TabFollowing ? getResources().getColor(R.color.colorTextPrimary) : getResources().getColor(R.color.colorTextSecondary));
        tvTabTop.setTypeface(id == R.id.textView_TabTop ? tfSemibold : tfRegular);
        tvTabAll.setTypeface(id == R.id.textView_TabAll ? tfSemibold : tfRegular);
        tvTabFollowing.setTypeface(id == R.id.textView_TabFollowing ? tfSemibold : tfRegular);
        llreviewContainer.setVisibility(View.VISIBLE);
    }

    private void fetchReview(){
        SqlHelper sqlHelper = new SqlHelper(ReviewsActivity.this, ReviewsActivity.this);
        sqlHelper.setActionString("reviews");
        sqlHelper.setExecutePath("get-review.php");
        sqlHelper.setMethod("GET");
        ContentValues params = new ContentValues();
        params.put("u_id", type.equals("movie") ? movieId : userId);
        params.put("c_id", MainActivity.currentUserModel.getUserId());
        params.put("type", type);
        params.put("seeker", "0");
        params.put("limit", "0");
        sqlHelper.setParams(params);
        sqlHelper.executeUrl(true);
    }

    private void initRecyclerView(JSONArray jsonArray){
        try{
            reviewModels = new ArrayList<>();
            reviewModelsFollowing = new ArrayList<>();
            reviewModelsTop = new ArrayList<>();
            globalReplyMask = new HashMap<>();
            int length = jsonArray.length();
            ModelHelper modelHelper = new ModelHelper(ReviewsActivity.this);
            for(int i = 1; i < length; i++){
                ReviewModel reviewModel = modelHelper.buildReviewModel(jsonArray.getJSONObject(i));
                reviewModel.setType(type);
                String reply = reviewModel.getReplies();
                if(reply.startsWith("#") || reply.isEmpty() || reply == null || reply == "null") {
                    reviewModels.add(reviewModel);
                    if(type.equals("movie")) {
                        if (reviewModel.getFollowing())
                            reviewModelsFollowing.add(reviewModel);
                        addToSortedList(reviewModel);
                        if (reply.startsWith("#"))
                            globalReplyMask.put(reviewModel.getReviewId(), reviewModels.size() - 1);
                    }
                }else{
                    reviewModel.setType("reply");
                    reviewModels.get(globalReplyMask.get(reply)).getRepliesList().add(reviewModel);
                }
            }
            attachAdapter(recyclerView, type.equals("movie") ? reviewModelsTop : reviewModels);
            toggleTab(R.id.textView_TabTop);
        }catch (Exception e){
            EmailHelper emailHelper = new EmailHelper(ReviewsActivity.this, EmailHelper.TECH_SUPPORT, "Error: ReviewsActivity", e.getMessage() + "\n" + StringHelper.convertStackTrace(e));
            emailHelper.sendEmail();
        }
    }

    public void addToSortedList(ReviewModel reviewModel){
        int length = reviewModelsTop.size();
        int likes = reviewModel.getLikes();
        for(int i = 0; i < length; i++){
            if(likes > reviewModelsTop.get(i).getLikes()){
                reviewModelsTop.add(i, reviewModel);
                return;
            }
        }
        reviewModelsTop.add(reviewModel);
    }

    public void attachAdapter(RecyclerView recyclerView, ArrayList<ReviewModel> model){
        try {
            LinearLayoutManager layoutManager = new LinearLayoutManager(ReviewsActivity.this, LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(layoutManager);
            ReviewAdapter reviewAdapter = new ReviewAdapter(ReviewsActivity.this, model, recyclerView);
            recyclerView.setAdapter(reviewAdapter);
        }catch (Exception e){
            EmailHelper emailHelper = new EmailHelper(ReviewsActivity.this, EmailHelper.TECH_SUPPORT, "Error: ReviewsActivity", e.getMessage() + "\n" + StringHelper.convertStackTrace(e));
            emailHelper.sendEmail();
        }
    }

    public void getUserDetails(String userId){
        SqlHelper sqlHelper = new SqlHelper(ReviewsActivity.this, ReviewsActivity.this);
        sqlHelper.setExecutePath("fetch-user.php");
        sqlHelper.setActionString("get_user");
        ContentValues params = new ContentValues();
        params.put("u_id", userId);
        params.put("c_id", MainActivity.currentUserModel.getUserId());
        sqlHelper.setMethod(getString(R.string.method_get));
        sqlHelper.setParams(params);
        sqlHelper.executeUrl(true);
    }
    public void toggleReviewContainer(){
        if(llreviewContainer.getVisibility() == View.VISIBLE){
            llreviewContainer.setVisibility(View.GONE);
        }
         else if(llreviewContainer.getVisibility() == View.GONE){
            llreviewContainer.setVisibility(View.VISIBLE);
        }
    }

    public void getMovieDetails(String movieId){
        SqlHelper sqlHelper = new SqlHelper(ReviewsActivity.this, ReviewsActivity.this);
        sqlHelper.setExecutePath("fetch-movie.php");
        sqlHelper.setActionString("get_movie");
        ContentValues params = new ContentValues();
        params.put("m_id", movieId);
        params.put("c_id", MainActivity.currentUserModel.getUserId());
        params.put("group_type", "individual");
        sqlHelper.setMethod(getString(R.string.method_get));
        sqlHelper.setParams(params);
        sqlHelper.executeUrl(true);
    }

    /***
     * Edits or Removes existing reviews
     * @param reviewId - ID of review
     * @param reviewText - Updated review
     * @param operation - Valid operations are "remove" and "update"
     */
    public void updateReview(String reviewId, String reviewText, String operation, String reply){
        SqlHelper sqlHelper = new SqlHelper(ReviewsActivity.this, ReviewsActivity.this);
        sqlHelper.setExecutePath("update-review.php");
        sqlHelper.setActionString("update_review:" + operation);
        ContentValues params = new ContentValues();
        params.put("r_id", reviewId);
        params.put("c_id", MainActivity.currentUserModel.getUserId());
        params.put("operation", operation);
        params.put("r_text", reviewText);
        params.put("reply", reply);
        sqlHelper.setMethod(getString(R.string.method_get));
        sqlHelper.setParams(params);
        sqlHelper.executeUrl(operation.equals("remove"));
    }
    public void submitReview(String type, String parentId, String reviewText){
        SqlHelper sqlHelper = new SqlHelper(ReviewsActivity.this, ReviewsActivity.this);
        sqlHelper.setExecutePath("add-review.php");
        sqlHelper.setActionString("add_review");
        ContentValues params = new ContentValues();
        params.put("m_id", movieId);
        params.put("u_id", MainActivity.currentUserModel.getUserId());
        params.put("p_id", parentId);
        params.put("type", type);
        params.put("r_text", reviewText);
        sqlHelper.setMethod(getString(R.string.method_get));
        sqlHelper.setParams(params);
        sqlHelper.executeUrl(false);
    }

    public void OnClick(Context context, ArrayList<ReviewModel> model, int position, View rootview, View view){
        switch (view.getId()){
            case R.id.textView_ReplyView:{
                LinearLayout linearLayout = rootview.findViewById(view.getId());
                linearLayout.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    /***
     *
     * @param userId- ID of User who reviewed
     * @param movieId- ID of Movie
     * @param reviewId- ID of Review
     * @param isLiked- True if current user has liked review, else False
     */
    public void updateReviewLike(int position, String userId, String movieId, String reviewId, boolean isLiked){
        SqlHelper sqlHelper = new SqlHelper(ReviewsActivity.this, ReviewsActivity.this);
        sqlHelper.setExecutePath("update-like.php");
        sqlHelper.setActionString("update_like");
        ContentValues params = new ContentValues();
        params.put("r_id", reviewId);
        params.put("u_id", userId);
        params.put("m_id", movieId);
        params.put("c_id", MainActivity.currentUserModel.getUserId());
        params.put("is_liked", "" + isLiked);
        sqlHelper.setMethod(getString(R.string.method_get));
        sqlHelper.setParams(params);
        HashMap<String, String> extras = new HashMap<>();
        extras.put("position", "" + position);
        sqlHelper.setExtras(extras);
        sqlHelper.executeUrl(false);
    }
}
