package bg.sasho.bustracker;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.accessory.SA;
import com.samsung.android.sdk.accessory.SAAgentV2;
import com.samsung.android.sdk.accessory.SAAuthenticationToken;
import com.samsung.android.sdk.accessory.SAPeerAgent;
import com.samsung.android.sdk.accessory.SASocket;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import bg.sasho.bustracker.database.BusStop;
import bg.sasho.bustracker.database.DatabaseHelper;

public class ProviderService extends SAAgentV2 {
    private static String URL = "https://api-arrivals.sofiatraffic.bg/api/v1/arrivals/%s/";

    private static final String TAG = "HelloAccessory(P)";
    private static final Class<ServiceConnection> SASOCKET_CLASS = ServiceConnection.class;
    private ServiceConnection mConnectionHandler = null;
    private Handler mHandler = new Handler();
    private Context mContext = null;
    private DatabaseHelper db;
    private List<BusStop> busStopsList = new ArrayList<>();


    public ProviderService(Context context) {
        super(TAG, context, SASOCKET_CLASS);
        mContext = context;

        db = new DatabaseHelper(context);


        SA mAccessory = new SA();
        try {
            mAccessory.initialize(mContext);
        } catch (SsdkUnsupportedException e) {
            // try to handle SsdkUnsupportedException
            if (processUnsupportedException(e) == true) {
                return;
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            /*
             * Your application can not use Samsung Accessory SDK. Your application should work smoothly
             * without using this SDK, or you may want to notify user and close your application gracefully
             * (release resources, stop Service threads, close UI thread, etc.)
             */
        }
    }

    @Override
    protected void onFindPeerAgentsResponse(SAPeerAgent[] peerAgents, int result) {
        Log.d(TAG, "onFindPeerAgentResponse : result =" + result);
    }

    @Override
    protected void onServiceConnectionRequested(SAPeerAgent peerAgent) {
        if (peerAgent != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, R.string.ConnectionAcceptedMsg, Toast.LENGTH_SHORT).show();
                }
            });
            acceptServiceConnectionRequest(peerAgent);

        }
    }

    @Override
    protected void onServiceConnectionResponse(SAPeerAgent peerAgent, SASocket socket, int result) {
        if (result == SAAgentV2.CONNECTION_SUCCESS) {
            if (socket != null) {
                mConnectionHandler = (ServiceConnection) socket;
            }
        } else if (result == SAAgentV2.CONNECTION_ALREADY_EXIST) {
            Log.e(TAG, "onServiceConnectionResponse, CONNECTION_ALREADY_EXIST");
        }
    }

    @Override
    protected void onAuthenticationResponse(SAPeerAgent peerAgent, SAAuthenticationToken authToken, int error) {
        /*
         * The authenticatePeerAgent(peerAgent) API may not be working properly depending on the firmware
         * version of accessory device. Please refer to another sample application for Security.
         */
    }

    @Override
    protected void onError(SAPeerAgent peerAgent, String errorMessage, int errorCode) {
        super.onError(peerAgent, errorMessage, errorCode);
    }

    private boolean processUnsupportedException(SsdkUnsupportedException e) {
        e.printStackTrace();
        int errType = e.getType();
        if (errType == SsdkUnsupportedException.VENDOR_NOT_SUPPORTED
                || errType == SsdkUnsupportedException.DEVICE_NOT_SUPPORTED) {
            /*
             * Your application can not use Samsung Accessory SDK. You application should work smoothly
             * without using this SDK, or you may want to notify user and close your app gracefully (release
             * resources, stop Service threads, close UI thread, etc.)
             */
        } else if (errType == SsdkUnsupportedException.LIBRARY_NOT_INSTALLED) {
            Log.e(TAG, "You need to install Samsung Accessory SDK to use this application.");
        } else if (errType == SsdkUnsupportedException.LIBRARY_UPDATE_IS_REQUIRED) {
            Log.e(TAG, "You need to update Samsung Accessory SDK to use this application.");
        } else if (errType == SsdkUnsupportedException.LIBRARY_UPDATE_IS_RECOMMENDED) {
            Log.e(TAG, "We recommend that you update your Samsung Accessory SDK before using this application.");
            return false;
        }
        return true;
    }

    public class ServiceConnection extends SASocket {
        public ServiceConnection() {
            super(ServiceConnection.class.getName());
        }

        @Override
        public void onError(int channelId, String errorMessage, int errorCode) {
        }

        @Override
        public void onReceive(int channelId, byte[] data) {
            if (mConnectionHandler == null) {
                return;
            }
            final String strToUpdateUI = new String(data);
            onDataAvailableonChannel(strToUpdateUI);

           /* JSONObject result = new JSONObject();
            try {
               // result = getStationTimes("1693");
                result = getNerbyStations();
            } catch (Exception e) {

            }



            String strToUpdateUI = new String(data);
            final String message = result.toString();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        mConnectionHandler.send(getServiceChannelId(0), message.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();*/
        }

        @Override
        protected void onServiceConnectionLost(int reason) {
            mConnectionHandler = null;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, R.string.ConnectionTerminateddMsg, Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void sendStationDetails(String data) {
            System.out.println("JSON received: " + data);
            try{
                JSONObject dataJSON = new JSONObject(data);
                String number = dataJSON.getString("number");
                JSONObject stop = getJSONObjectFromURL(String.format(URL, number));

                stop.remove("license");
                stop.remove("timestamp_calculated");
                JSONObject result = new JSONObject();
                result.put("msgId", Constraints.STOP_DETAILS_RSP);
                result.put("stop", stop);
                System.out.println("JSON3: " + result.toString());
                sendResponse(result.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }




        }

        private void sendStations(String data) {
            JSONObject result = new JSONObject();
            JSONArray array = new JSONArray();
            for(BusStop stop:db.getAllBusStops()) {
                JSONObject stopToAdd = new JSONObject();

                try {
                    stopToAdd.put("name", stop.getName());
                    stopToAdd.put("number", stop.getNumber());
                    stopToAdd.put("officialName", stop.getOfficialName());


                } catch (JSONException e) {
                    Log.i("error", e.getMessage());
                }
                array.put(stopToAdd);
            }
            try {
                result.put("msgId", Constraints.STOPS_LIST_RSP);
                result.put("stops", array);
                sendResponse(result.toString());
            } catch (JSONException e) {
                Log.i("error", e.getMessage());
            }

        }

        public void sendResponse(String response) {
            if (mConnectionHandler == null) {
                return;
            }
            System.out.println("Send on SAP: " + response);
            final String message = response;
            new Thread(new Runnable() {
                public void run() {
                    try {
                        mConnectionHandler.send(getServiceChannelId(0), message.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        private void onDataAvailableonChannel(final String data) {

                    if (data.contains(Constraints.STOP_DETAILS_REQ)) {

                            sendStationDetails(data);


                    } else if (data.contains(Constraints.STOPS_LIST_REQ)) {
                        sendStations(data);
                    }


        }

        public JSONObject getJSONObjectFromURL(String urlString) throws IOException, JSONException {
            HttpURLConnection urlConnection = null;
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setDoOutput(true);
            urlConnection.connect();

            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();

            String jsonString = sb.toString();
            System.out.println("JSON: " + jsonString);

            return new JSONObject(jsonString);
        }
    }
}
