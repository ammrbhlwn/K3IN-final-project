//package com.example.final_project;
//
//import android.os.Build;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.navigation.NavController;
//import androidx.navigation.Navigation;
//
//import com.example.calmscope.R;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import java.io.BufferedReader;
//import java.io.DataOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.util.Base64;
//
//public class LoadJsonFragment extends Fragment {
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_loadjson, container, false);
//
//        String imageFilePath = requireArguments().getString("imageFilePath");
//
//        Bundle bundle = new Bundle();
//        bundle.putString("imageFilePath", imageFilePath);
//
//        new Thread(() -> {
//            try {
//                File file = new File(imageFilePath);
//
//                // Base64 Encode the image
//                String encodedFile = "";
//                FileInputStream fileInputStreamReader = new FileInputStream(file);
//                byte[] bytes = new byte[(int) file.length()];
//                fileInputStreamReader.read(bytes);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    encodedFile = Base64.getEncoder().encodeToString(bytes);
//                }
//
//                String uploadURL = "https://detect.roboflow.com/calmscope-slnhz/1?api_key=gHAEteoBBc8llY5NZkgV&name=" + file.getName();
//
//                URL url = new URL(uploadURL);
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestMethod("POST");
//                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//                connection.setDoOutput(true);
//
//                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
//                wr.writeBytes(encodedFile);
//                wr.flush();
//                wr.close();
//
//                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                StringBuilder response = new StringBuilder();
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    response.append(line).append("\n");
//                }
//                reader.close();
//
//                String jsonResponse = response.toString();
//                JSONObject jsonObject = new JSONObject(jsonResponse);
//                JSONArray predictions = jsonObject.getJSONArray("predictions");
//
//                if (predictions.length() > 0) {
//                    JSONObject firstPrediction = predictions.getJSONObject(0);
//                    String detectedClass = firstPrediction.getString("class");
//
//                    // Navigate based on class
//                    requireActivity().runOnUiThread(() -> {
//                        NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment);
//                        if ("stress".equalsIgnoreCase(detectedClass)) {
//                            navController.navigate(R.id.sadFragment, bundle);
//                        } else {
//                            navController.navigate(R.id.neutralFragment, bundle);
//                        }
//                    });
//                } else {
//                    requireActivity().runOnUiThread(() ->
//                            Toast.makeText(requireContext(), "No predictions found.", Toast.LENGTH_LONG).show()
//                    );
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment);
//                navController.navigate(R.id.errorFragment);
//                requireActivity().runOnUiThread(() ->
//                        Toast.makeText(requireContext(), "Error during analysis", Toast.LENGTH_LONG).show()
//                );
//            }
//        }).start();
//
//
//        return view;
//    }
//}