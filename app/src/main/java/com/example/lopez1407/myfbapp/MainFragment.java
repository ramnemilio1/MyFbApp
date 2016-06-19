package com.example.lopez1407.myfbapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;


/**
 * Created by lopez1407 on 6/11/2016.
 */
public class MainFragment extends Fragment {

    private TextView mTextDetails;
    private TextView mTextPostDetails;
    private ImageView mPostPicture;
    private ProfilePictureView mProfilePicture;
    private CallbackManager mCallbackManager;
    private FacebookCallback<LoginResult> mCallback;
    private String postID;

public MainFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
        mCallback = new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                Profile profile = Profile.getCurrentProfile();


                if(profile!=null) {
                    mTextDetails.setText("Welcome " + profile.getName());


                    //getting User Profile Picture
                    mProfilePicture.setProfileId(profile.getId());

                    //getting User Post list
                    new GraphRequest(
                            AccessToken.getCurrentAccessToken(),
                            "/LaMediaCancha/posts",
                            null,
                            HttpMethod.GET,
                            new GraphRequest.Callback() {
                                public void onCompleted(GraphResponse response) {

                                    try {

                                        JSONObject json = response.getJSONObject();
                                        JSONArray jarray = json.getJSONArray("data");
                                        for(int i = 0; i < 1; i++){
                                             JSONObject obj = jarray.getJSONObject(i);
                                             postID= obj.optString("id");
                                             getPostByID(postID);
                                        }


                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }


                                }
                            }
                    ).executeAsync();

                }

            }

            @Override
            public void onCancel() {
                mTextDetails.setText("CANCELL");
            }

            @Override
            public void onError(FacebookException error) {
                mTextDetails.setText("ERROR: " + error.toString());
            }
        };

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
        mTextDetails = (TextView) view.findViewById(R.id.text_details);
        mTextPostDetails = (TextView) view.findViewById(R.id.text_postDetails);
        mProfilePicture =  (ProfilePictureView)view.findViewById(R.id.image_profilePicture);
        mPostPicture =  (ImageView)view.findViewById(R.id.image_post);

        //permissions
        loginButton.setReadPermissions("user_posts");

        loginButton.setFragment(this);
        loginButton.registerCallback(mCallbackManager, mCallback);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * Populate the textView and ImageView with the information from post specified
     * @param postID
     */
    public void getPostByID(String postID){

        GraphRequest request = GraphRequest.newGraphPathRequest(
                AccessToken.getCurrentAccessToken(),
                postID,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        try {

                           // mTextPostDetails.setText(response.getRawResponse());
                            JSONObject obj =  response.getJSONObject();
                            String imageURL= obj.optString("full_picture");
                            String message= obj.optString("message");

                            mTextPostDetails.setText(message);
                            new DownloadImageTask(mPostPicture)
                                .execute(imageURL);


                        } catch (Exception e) {
                            mTextDetails.setText("ERROR: "+e.toString());
                            e.printStackTrace();
                        }
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "full_picture,message");
        request.setParameters(parameters);
        request.executeAsync();


    }


    /**
     * Download a image and populate a  ImageView specified
     */
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }






}
