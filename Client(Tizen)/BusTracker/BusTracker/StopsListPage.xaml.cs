namespace BusTracker
{
    using Xamarin.Forms;
    using Xamarin.Forms.Xaml;
    using System.Collections.Generic;
    using System.Collections.ObjectModel;
    using Tizen.Wearable.CircularUI.Forms;
    using Models;

    [XamlCompilation(XamlCompilationOptions.Compile)]
    public partial class StopsListPage : CirclePage
    {
        private ObservableCollection<StopList> stopsListObservableCollection;


        public StopsListPage(List<StopList> stopsList)
        {

            InitializeComponent();
            stopsListObservableCollection = new ObservableCollection<StopList>(stopsList);
            stopsListView.ItemsSource = stopsListObservableCollection;
            stopsListView.ItemAppearing += ListView_ItemAppearing;
            stopsListView.ItemTapped += ListView_ItemTapped;


        }

        public void AddNextStops(List<StopList> imageList)
        {
            imageList.ForEach(m => stopsListObservableCollection.Add(m));
        }
        private void ListView_ItemTapped(object sender, ItemTappedEventArgs e)
        {
            SAPManager.FetchStop((e.Item as StopList).Number);
        }

        private void ListView_ItemAppearing(object sender, ItemVisibilityEventArgs e)
        {

        }
    }
}