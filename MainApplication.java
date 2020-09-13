package com.admob;

import android.app.Application;

import com.admob.googlepay.RNGooglePayPackage;
import com.facebook.react.ReactApplication;
import com.reactnativepayments.ReactNativePaymentsPackage;
import com.rnfs.RNFSPackage;

import fr.greweb.reactnativeviewshot.RNViewShotPackage;

import cl.json.RNSharePackage;
import cl.json.ShareApplication;

import com.swmansion.gesturehandler.react.RNGestureHandlerPackage;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;

import java.util.Arrays;
import java.util.List;

public class MainApplication extends Application implements ReactApplication, ShareApplication {

    private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {
        @Override
        public boolean getUseDeveloperSupport() {
            return BuildConfig.DEBUG;
        }

        @Override
        protected List<ReactPackage> getPackages() {
            return Arrays.<ReactPackage>asList(
                    new MainReactPackage(),
                    new ReactNativePaymentsPackage(),
                    new RNFSPackage(),
                    new RNViewShotPackage(),
                    new RNSharePackage(),
                    new RNGestureHandlerPackage(),
                    new RNGooglePayPackage()
            );
        }

        @Override
        protected String getJSMainModuleName() {
            return "index";
        }
    };

    @Override
    public ReactNativeHost getReactNativeHost() {
        return mReactNativeHost;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SoLoader.init(this, /* native exopackage */ false);
    }

    @Override
    public String getFileProviderAuthority() {
        return BuildConfig.APPLICATION_ID + ".provider";
    }
}
