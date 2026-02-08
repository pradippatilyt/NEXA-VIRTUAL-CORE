package com.nexa.awesome.utils.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

public class RealAccountTokenFetcher {
    public static String fetch(Context context, Account account, String tokenType) {
        try {
            AccountManager am = AccountManager.get(context);
            Bundle result = am.getAuthToken(account, tokenType, null, false, null, null).getResult();
            if (result != null) {
                return result.getString(AccountManager.KEY_AUTHTOKEN);
            }
        } catch (Exception e) {
            Log.e("RealAccountTokenFetcher", "Error fetching token", e);
        }
        return null;
    }
}