namespace BusTracker
{
    using System;
    using System.Diagnostics;
    using System.Linq;
    using System.Text;
    using Models;
    using Newtonsoft.Json;
    using Samsung.Sap;
    using Tizen.Wearable.CircularUI.Forms;
    using Xamarin.Forms;
    public static class SAPManager
    {
        private static Agent agent;
        private static Peer peer;
        private static Connection connection;
        private static StopsListPage stopListPage;

        static SAPManager()
        {
            agent = Agent.GetAgent("/sofia/bustracker").Result;
        }

        public static async void Connect()
        {
            try
            {
                var peers = await agent.FindPeers();
                if (peers.Count() > 0)
                {
                    peer = peers.First();
                    connection = peer.Connection;
                    connection.DataReceived -= Connection_DataReceived;
                    connection.DataReceived += Connection_DataReceived;
                    connection.StatusChanged -= Connection_StatusChanged;
                    connection.StatusChanged += Connection_StatusChanged;
                    await connection.Open();
                }
                else
                {
                    Debug.WriteLine("[DEBUG] Any peer not found");
                }
            }
            catch (Exception ex)
            {
                Debug.WriteLine($"[DEBUG] {ex.Message}");
            }
        }

        private static void Connection_StatusChanged(object sender, ConnectionStatusEventArgs e)
        {
            Toast.DisplayText(e.Reason.ToString());

            if (e.Reason == ConnectionStatus.ConnectionClosed ||
                e.Reason == ConnectionStatus.ConnectionLost)
            {
                connection.DataReceived -= Connection_DataReceived;
                connection.StatusChanged -= Connection_StatusChanged;
                connection.Close();
                connection = null;
                peer = null;
            }
        }

        private static void Connection_DataReceived(object sender, Samsung.Sap.DataReceivedEventArgs e)
        {
            string json = Encoding.UTF8.GetString(e.Data);
            if (json.Contains(Constraints.StopDetailsRsp))
            {
                var response = JsonConvert.DeserializeObject<StopDetailsResponse>(json);
                var page = new StopDetailsPage(response.Stop);
                (App.Current.MainPage as NavigationPage).PushAsync(page);
            }
            else if (json.Contains(Constraints.StopsListRsp))
            {
                var response = JsonConvert.DeserializeObject<StopsListResponse>(json);

                stopListPage = new StopsListPage(response.Stops);
                (App.Current.MainPage as NavigationPage).PushAsync(stopListPage);

            }
        }

        public static void Disconnect()
        {
            if (connection != null)
            {
                connection.Close();
            }
        }

        public static void FetchStopsList()
        {
            if (connection != null)
            {
                var json = JsonConvert.SerializeObject(new StopsListRequest());
                connection.Send(agent.Channels.First().Value, Encoding.UTF8.GetBytes(json));
            }
        }

        public static void FetchStop(string number)
        {
            if (connection != null)
            {
                var json = JsonConvert.SerializeObject(new StopDetailsRequest(number));
                connection.Send(agent.Channels.First().Value, Encoding.UTF8.GetBytes(json));
            }
        }
    }
}