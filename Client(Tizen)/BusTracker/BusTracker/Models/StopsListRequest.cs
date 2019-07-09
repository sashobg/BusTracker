namespace BusTracker.Models
{
    using Newtonsoft.Json;
    public class StopsListRequest
    {
        public enum StopsFetchType
        {
            All = 1,
            NearBy = 2,
            Favourites = 3
        }

        [JsonProperty("stopsType")]
        public int StopsType { get; set; }

        [JsonProperty("msgId")]
        public string MessageId { get; set; }

        public StopsListRequest()
        {
            MessageId = Constraints.StopsListReq;
        }
    }
}