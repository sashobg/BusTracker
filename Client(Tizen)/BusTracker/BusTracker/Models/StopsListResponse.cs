namespace BusTracker.Models
{
    using Newtonsoft.Json;
    using System.Collections.Generic;
    public class StopsListResponse
    {
        [JsonProperty("msgId")]
        public string MessageId { get; set; }

        [JsonProperty("stops")]
        public List<StopList> Stops { get; set; }
    }
}