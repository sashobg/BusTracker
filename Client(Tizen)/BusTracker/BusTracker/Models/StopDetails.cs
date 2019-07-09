using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text;
using BusTracker.Models;
using Newtonsoft.Json;
using Xamarin.Forms;

namespace BusTracker.Models
{
    using System.Collections.Generic;

    public class Arrival
    {
        public string time { get; set; }
        public bool has_air_conditioning { get; set; }
        public object has_wifi { get; set; }
        public bool is_wheelchair_accessible { get; set; }
    }

    public class Line
    {
        [JsonProperty("vehicle_type")]
        public string vehicleType { get; set; }
        

        public string colorText
        {
            get
            {
                switch (vehicleType)
                {
                    case "tram": return "#FF8C00"; break;
                    case "trolley": return "#2F2F84"; break;
                    case "bus": return "#D82330"; break;
                       default: return "White";
                }
            }
        }

        public List<Arrival> arrivals { get; set; }
    
        public string allArrivals
        {
            get
            {
                return allArrivalsToString();
            }
        }
        

        public object direction { get; set; }
        public string name { get; set; }

        private string allArrivalsToString()
        {
            StringBuilder sb = new StringBuilder();
            foreach (var arrival in arrivals)
            {
                sb.Append(arrival.time);

                if (arrival.has_air_conditioning)
                {
                    sb.AppendLine(Char.ConvertFromUtf32(0x2744));
                }
                else
                {
                    sb.AppendLine();
                }
            }

            return sb.ToString();
        }

    }

    public class StopDetails 
    {
        
        public string code { get; set; }
        public List<Line> lines { get; set; }
        //    public string timestamp_calculated { get; set; }
        [JsonProperty("name")]
        public string Name { get; set; }
        
    }
}