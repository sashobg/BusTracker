namespace BusTracker.Models
{
    using Newtonsoft.Json;
    public class StopDetailsResponse
    {
        [JsonProperty("msgId")]
        public string MessageId { get; set; }

        [JsonProperty("stop")]
        public StopDetails Stop { get; set; }
    }
}