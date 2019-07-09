namespace BusTracker.Models
{
    using Newtonsoft.Json;

    public class StopDetailsRequest
    {
        [JsonProperty("msgId")]
        public string MessageId { get; set; }

        [JsonProperty("number")]
        public string Number { get; set; }

        public StopDetailsRequest(string number)
        {
            MessageId = Constraints.StopDetailsReq;
            Number = number;
        }
    }
}