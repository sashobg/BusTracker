using System;
using BusTracker.Models;
using Xamarin.Forms;

namespace BusTracker
{
    using Tizen.Wearable.CircularUI.Forms;
    using Xamarin.Forms.Xaml;

    [XamlCompilation(XamlCompilationOptions.Compile)]
    public partial class StopDetailsPage : CirclePage
    {

        private string stopNumber;

        public StopDetailsPage(StopDetails stop)
        {
            InitializeComponent();
            stopNumber = stop.code;
            stopDetailsView.BindingContext = stop;
            stopDetailsView.ItemTapped += DetailView_ItemTapped;

        }

        private void DetailView_ItemTapped(object sender, ItemTappedEventArgs e)
        {
            SAPManager.FetchStop(stopNumber);
        }
    }
}