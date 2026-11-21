package com.kinclong.util;

import com.kinclong.model.Karyawan;

public class SessionManager {
    private static Karyawan currentUser;

    public static void setCurrentUser(Karyawan karyawan) {
        currentUser = karyawan;
    }

    public static Karyawan getCurrentUser() {
        return currentUser;
    }

    public static void clearSession() {
        currentUser = null;
    }
} 
    
