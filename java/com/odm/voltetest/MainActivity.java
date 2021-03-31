package com.odm.voltetest;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.ims.ImsManager;
import android.telephony.ims.ImsMmTelManager;
import android.util.Log;
import android.widget.Switch;

import java.util.List;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.telephony.ims.feature.ImsFeature;
import android.telecom.TelecomManager;
import android.telephony.ims.ImsException;
import android.content.Intent;

public class MainActivity extends Activity {

    private static final String TAG = "VoLTE";

    private Switch voLTE;
    private TelephonyManager mTelephonyManager;
    private SubscriptionManager mSubscriptionManager;
    private int mSubId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mSubscriptionManager = (SubscriptionManager) getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

        initView();
        initAction();
    }

    @Override   
    protected void onResume() {
        super.onResume();
        try {
            CarrierConfigManager configManager = (CarrierConfigManager) this.getSystemService(Context.CARRIER_CONFIG_SERVICE);
            PersistableBundle b = configManager.getConfig();
            if(b != null) {
                boolean hideVolte = b.getBoolean(CarrierConfigManager.KEY_HIDE_ENHANCED_4G_LTE_BOOL);                                       
                boolean editableVolte = b.getBoolean(CarrierConfigManager.KEY_EDITABLE_ENHANCED_4G_LTE_BOOL);
                android.util.Log.i("ccg", "hideVolte: " + hideVolte + "editableVolte: " + editableVolte);
                voLTE.setChecked(hideVolte);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        voLTE = (Switch) findViewById(R.id.volte);
        voLTE.setEnabled(false);
        if (hasSimCard()) {
            if (mSubscriptionManager != null) {
                List<SubscriptionInfo> subscriptionInfoList = mSubscriptionManager.getActiveSubscriptionInfoList();
                for (SubscriptionInfo s:subscriptionInfoList
                     ) {
                    Log.d("ccg", "SubscriptionInfo id " + s.getSubscriptionId());
                    mSubId = s.getSubscriptionId();
                }
            }
            boolean isAvailable = getAvailabilityStatus(mSubId);
            Log.d("ccg", "isAvailable=" + isAvailable);
            if (isAvailable) {
                voLTE.setEnabled(true);
                // voLTE.setChecked(isVolteEnabled());
            }
        }
    }

    private void initAction() {
        voLTE.setOnClickListener(v -> {
            Log.d("ccg", "voLTE.setOnClickListener");
            /*if (!SubscriptionManager.isValidSubscriptionId(mSubId)) {
                Log.d("ccg", "is not Valid SubscriptionId");
                return;
            }
            // volte开关功能
            final ImsMmTelManager imsMmTelManager = ImsMmTelManager.createForSubscriptionId(mSubId);
            if (imsMmTelManager == null) {
                Log.d("ccg", "imsMmTelManager == null");
                return;
            }
            boolean isChecked = voLTE.isChecked();
            Log.d("ccg", "isChecked=" + isChecked);

            setAdvancedCallingSettingEnabled(imsMmTelManager, isChecked);*/
            boolean isShow = voLTE.isChecked();
            try {
                CarrierConfigManager configManager = (CarrierConfigManager) this.getSystemService(Context.CARRIER_CONFIG_SERVICE);
                PersistableBundle b = configManager.getConfig();
                if(b != null) {
                    boolean hideVolte = b.getBoolean(CarrierConfigManager.KEY_HIDE_ENHANCED_4G_LTE_BOOL);                                       
                    boolean editableVolte = b.getBoolean(CarrierConfigManager.KEY_EDITABLE_ENHANCED_4G_LTE_BOOL);
                    android.util.Log.i("ccg", "hideVolte: " + hideVolte + "editableVolte: " + editableVolte);

                    if(isShow) {
                        b.putBoolean(CarrierConfigManager.KEY_HIDE_ENHANCED_4G_LTE_BOOL, false);
                        b.putBoolean(CarrierConfigManager.KEY_EDITABLE_ENHANCED_4G_LTE_BOOL, true);
                    } else {
                        b.putBoolean(CarrierConfigManager.KEY_HIDE_ENHANCED_4G_LTE_BOOL, true);
                        b.putBoolean(CarrierConfigManager.KEY_EDITABLE_ENHANCED_4G_LTE_BOOL, false);
                    }

                    configManager.overrideConfig(android.telephony.SubscriptionManager.getDefaultSubscriptionId(), b);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private boolean setAdvancedCallingSettingEnabled(ImsMmTelManager imsMmTelManager,
                                                     boolean isChecked) {
        try {
            imsMmTelManager.setAdvancedCallingSettingEnabled(isChecked);
        } catch (IllegalArgumentException exception) {
            Log.w(TAG, "fail to set VoLTE=" + isChecked + ". subId=" + mSubId, exception);
            return false;
        }
        return true;
    }

    public boolean isVolteEnabled() {
        SubscriptionInfo subscriptionInfo = null;
        if (mSubscriptionManager != null) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
            }
            List<SubscriptionInfo> subscriptionInfoList = mSubscriptionManager.getActiveSubscriptionInfoList();
            if (subscriptionInfoList == null) {
                return false;
            }
            for (SubscriptionInfo s:subscriptionInfoList
                 ) {
                Log.d("ccg", "SubscriptionInfo id " + s.getSubscriptionId());
                subscriptionInfo = s;
                // mSubId = subscriptionInfo.getSubscriptionId();
            }
        }
        final int subId = subscriptionInfo != null ? subscriptionInfo.getSubscriptionId()
                          : SubscriptionManager.INVALID_SUBSCRIPTION_ID;
        int phoneId = SubscriptionManager.getPhoneId(subId);
        boolean isVoLTEEnabled = ImsManager.getInstance(getApplicationContext(), phoneId)
                          .isEnhanced4gLteModeSettingEnabledByUser();
        Log.d("ccg", "isVoLTEEnabled = " + isVoLTEEnabled);
        return isVoLTEEnabled;
    }

    private boolean hasSimCard() {
        if (mTelephonyManager == null) {
            mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        }
        int simState = mTelephonyManager.getSimState();
        boolean result = true;
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
                result = false;
                break;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                result = false;
                break;
            default:
        }
        return result;
    }

    public boolean getAvailabilityStatus(int subId) {
        final PersistableBundle carrierConfig = getCarrierConfigForSubId(subId);
        if ((carrierConfig == null)) {
            return false;
        }
        try {
            if (!isServiceStateReady(subId)) {
                return false;
            }
        } catch (InterruptedException e) {
            Log.d("ccg", "e " + e);
        } catch (ImsException e) {
            Log.d("ccg", "e " + e);
        } catch (IllegalArgumentException e) {
            Log.d("ccg", "e " + e);
        }
        return isAllowUserControl();
    }

    public PersistableBundle getCarrierConfigForSubId(int subId) {
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            return null;
        }
        final CarrierConfigManager carrierConfigMgr =
                this.getSystemService(CarrierConfigManager.class);
        return carrierConfigMgr.getConfigForSubId(subId);
    }

    boolean isServiceStateReady(int subId) throws InterruptedException, ImsException,
            IllegalArgumentException {
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            return false;
        }   

        final ImsMmTelManager imsMmTelManager = ImsMmTelManager.createForSubscriptionId(subId);
        // TODO: have a shared thread pool instead of create ExecutorService
        //       everytime to improve performance.
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final IntegerConsumer intResult = new IntegerConsumer();                                                                      
        imsMmTelManager.getFeatureState(executor, intResult);
        return (intResult.get(2000L) == ImsFeature.STATE_READY);
    }

    /** 
     * Get allowance status for user to alter configuration
     *
     * @return true when changing configuration by user is allowed.
     */
    public boolean isAllowUserControl() {                                                                                             
        if (!SubscriptionManager.isValidSubscriptionId(mSubId)) {
            return false;
        }

        return ((!isTtyEnabled(this))
                || (isTtyOnVolteEnabled(mSubId)));
    }  

    boolean isTtyEnabled(Context context) {
        final TelecomManager telecomManager = context.getSystemService(TelecomManager.class);
        return (telecomManager.getCurrentTtyMode() != TelecomManager.TTY_MODE_OFF);
    }

    boolean isTtyOnVolteEnabled(int subId) {                                                                                          
        try {
            final ImsMmTelManager imsMmTelManager =
                    ImsMmTelManager.createForSubscriptionId(mSubId);
            return imsMmTelManager.isTtyOverVolteEnabled();
        } catch (IllegalArgumentException exception) {
            Log.w("ccg", "fail to get VoLte Tty Stat. subId=" + mSubId, exception);
        }                                                                                                                             
        return false;
    }
}
