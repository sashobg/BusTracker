using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Xamarin.Forms;
using Xamarin.Forms.Xaml;

namespace BusTracker
{
    
    public partial class App : Application
    {
        public App()
        {
            MainPage = new NavigationPage(new MainPage());
        }

    }
}
