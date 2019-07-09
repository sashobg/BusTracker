namespace BusTracker.Models
{
    using Newtonsoft.Json;
    public class StopList
    {
        [JsonProperty("officialName")]
        public string Name { get; set; }
        [JsonProperty("name")]
        public string CustomName { get; set; }
        [JsonProperty("number")]
        public string Number { get; set; }

    }
}