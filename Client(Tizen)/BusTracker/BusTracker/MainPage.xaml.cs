using Xamarin.Forms;
using Xamarin.Forms.Xaml;
using Tizen.Wearable.CircularUI.Forms;

namespace BusTracker
{
    [XamlCompilation(XamlCompilationOptions.Compile)]
    public partial class MainPage : CirclePage
    {
        public MainPage()
        {
            SAPManager.Connect();
            InitializeComponent();
            listView.ItemTapped += ListView_ItemTapped;
        }

        private void ListView_ItemTapped(object sender, ItemTappedEventArgs e)
        {
            switch (e.Item as string)
            {
                case "Connect":
                    SAPManager.Connect();
                    break;

                case "Favourites":
                    SAPManager.FetchStopsList();
                    break;

                case "Close":
                    SAPManager.Disconnect();
                    break;
            }
        }
    }
}