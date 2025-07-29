# Watch & Earn - Android App

A complete Android application built in Kotlin where users can watch rewarded video ads and earn money. The app features Firebase authentication, Firestore database, AdMob integration, and a referral system.

## 🚀 Features

### User Features
- **Authentication**: Email/password sign up and login with Firebase Auth
- **Dashboard**: Clean, modern UI showing coin balance and earning options
- **Watch Ads**: Rewarded video ads integration with AdMob (earn 30 coins per ad)
- **Daily Bonus**: 10 coins daily bonus system
- **Referral System**: Share referral codes and earn 200 coins per successful referral
- **Withdrawal**: Request withdrawals with minimum 1000 coins ($0.10 USD)
- **Transaction History**: Complete history of all earnings and transactions
- **Profile Management**: Update display name and manage account

### Technical Features
- **Firebase Authentication**: Secure user authentication
- **Firestore Database**: Real-time data storage and synchronization
- **AdMob Integration**: Google Mobile Ads SDK for rewarded videos
- **Material Design**: Modern, dark-themed UI with gradient accents
- **Revenue Sharing**: 30% for viewers, 70% for app owner
- **Conversion Rate**: 10,000 coins = $1.00 USD

## 📱 Screenshots

The app features a modern dark theme with green gradient accents, clean typography, and intuitive navigation.

## 🛠️ Setup Instructions

### Prerequisites
1. Android Studio (latest version)
2. Firebase project
3. Google AdMob account
4. Google Play Console account (for publishing)

### Firebase Setup

1. **Create Firebase Project**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Create a new project
   - Add an Android app with package name: `com.example.watchandearn`

2. **Enable Firebase Services**
   - **Authentication**: Enable Email/Password provider
   - **Firestore**: Create database in production mode
   - **Cloud Messaging**: Enable for notifications (optional)

3. **Download Configuration**
   - Download `google-services.json` from Firebase Console
   - Place it in `app/` directory

4. **Firestore Security Rules**
   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       // Users can read/write their own data
       match /users/{userId} {
         allow read, write: if request.auth != null && request.auth.uid == userId;
       }
       
       // Users can read/write their own transactions
       match /transactions/{transactionId} {
         allow read, write: if request.auth != null && 
           resource.data.userId == request.auth.uid;
       }
       
       // Users can create withdrawal requests
       match /withdrawalRequests/{requestId} {
         allow create: if request.auth != null;
         allow read: if request.auth != null && 
           resource.data.userId == request.auth.uid;
       }
     }
   }
   ```

### AdMob Setup

1. **Create AdMob Account**
   - Go to [AdMob Console](https://admob.google.com/)
   - Create an account and add your app

2. **Get Ad Unit IDs**
   - Create a Rewarded Ad unit
   - Copy the Ad Unit ID
   - Update `Constants.kt` with your actual IDs:
   ```kotlin
   const val ADMOB_APP_ID = "ca-app-pub-YOUR_PUBLISHER_ID~YOUR_APP_ID"
   const val REWARDED_AD_UNIT_ID = "ca-app-pub-YOUR_PUBLISHER_ID/YOUR_AD_UNIT_ID"
   ```

3. **Update AndroidManifest.xml**
   ```xml
   <meta-data
       android:name="com.google.android.gms.ads.APPLICATION_ID"
       android:value="ca-app-pub-YOUR_PUBLISHER_ID~YOUR_APP_ID" />
   ```

### Building the App

1. **Clone/Download the project**
2. **Open in Android Studio**
3. **Sync Gradle files**
4. **Add your Firebase configuration**
5. **Update AdMob IDs**
6. **Build and run**

### Generating APK

1. **Debug APK**
   ```bash
   ./gradlew assembleDebug
   ```

2. **Release APK**
   - Generate signing key:
   ```bash
   keytool -genkey -v -keystore my-release-key.keystore -alias my-key-alias -keyalg RSA -keysize 2048 -validity 10000
   ```
   - Build signed APK:
   ```bash
   ./gradlew assembleRelease
   ```

## 📊 Database Structure

### Users Collection
```javascript
{
  "userId": {
    "email": "user@example.com",
    "displayName": "User Name",
    "coinBalance": 1250,
    "referralCode": "ABC12345",
    "createdAt": 1640995200000,
    "totalEarnings": 12.50
  }
}
```

### Transactions Collection
```javascript
{
  "transactionId": {
    "userId": "user_id",
    "type": "ad_watch", // ad_watch, daily_bonus, referral_bonus, withdrawal
    "amount": 30,
    "timestamp": 1640995200000,
    "description": "Earned 30 coins by watching ad"
  }
}
```

### Withdrawal Requests Collection
```javascript
{
  "requestId": {
    "userId": "user_id",
    "walletId": "user@paypal.com",
    "coinAmount": 1000,
    "dollarAmount": 0.10,
    "status": "pending", // pending, approved, rejected
    "requestDate": 1640995200000,
    "processedDate": null
  }
}
```

## 🔧 Configuration

### Revenue Settings
- **Viewer Share**: 30% of ad revenue
- **Owner Share**: 70% of ad revenue
- **Coin Values**:
  - Ad Watch: 30 coins
  - Daily Bonus: 10 coins
  - Referral Bonus: 200 coins
- **Conversion Rate**: 10,000 coins = $1.00 USD
- **Minimum Withdrawal**: 1,000 coins ($0.10 USD)

### Customization
You can easily customize the app by modifying:
- `Constants.kt`: Coin values, conversion rates, AdMob IDs
- `colors.xml`: App color scheme
- `strings.xml`: App text and messages
- `themes.xml`: UI styling and themes

## 🚀 Publishing to Play Store

1. **Prepare for Release**
   - Update app version in `build.gradle`
   - Generate signed APK
   - Test thoroughly on different devices

2. **Play Store Requirements**
   - App privacy policy (required for apps with user data)
   - App content rating
   - Store listing (screenshots, description, etc.)

3. **AdMob Policy Compliance**
   - Ensure ads don't encourage invalid clicks
   - Follow AdMob policies for rewarded ads
   - Implement proper ad loading and error handling

## 🛡️ Security Considerations

- Firebase security rules are properly configured
- User data is protected and encrypted
- Ad fraud prevention measures in place
- Withdrawal requests require manual approval

## 📞 Support & Admin Features

### Admin Panel (Firebase Console)
Admins can manage the app through Firebase Console:

1. **User Management**
   - View user profiles and balances
   - Monitor user activity

2. **Withdrawal Management**
   - Query withdrawal requests:
   ```javascript
   // Get pending withdrawals
   db.collection('withdrawalRequests')
     .where('status', '==', 'pending')
     .get()
   ```
   - Approve/reject requests by updating status field

3. **Analytics**
   - Monitor app usage through Firebase Analytics
   - Track ad performance and revenue

## 🔄 Updates and Maintenance

- Regular updates for security patches
- Monitor Firebase usage and costs
- Update AdMob settings as needed
- Review and process withdrawal requests
- Monitor app performance and user feedback

## 📄 License

This project is created for educational and commercial purposes. Make sure to comply with:
- Google Play Store policies
- AdMob policies and terms
- Firebase terms of service
- Local regulations regarding digital payments

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

---

**Note**: This app is ready for production but requires proper Firebase and AdMob configuration. Make sure to test all features thoroughly before publishing to the Play Store.
