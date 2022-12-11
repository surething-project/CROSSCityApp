<p align="center">
  <img src="./app/src/main/ic_launcher-playstore.png" width="200" height="200" alt="CROSS Logo"/>
</p>

<h3 align="center">CROSS City</h3>
<h4 align="center">Mobile Application</h4>

<p align="center"><i>loCation pROof techniqueS for consumer mobile applicationS</i></p>

---

This is the repository that contains the main contribution of my ([@RicardoGrade](https://github.com/RicardoGrade)) dissertation -- *CROSS City Mobile Application: Gamified Peer-Based Location Certification*, the code for my P2P Witnessing strategy can be found [here](https://github.com/inesc-id/CROSSCityApp/tree/main/app/src/main/java/pt/ulisboa/tecnico/cross/peertopeer), and the code for user-user communication via Bluetooth Low Energy can be found [here](https://github.com/inesc-id/CROSSCityApp/tree/main/app/src/main/java/pt/ulisboa/tecnico/cross/ble). In addition to this repository, I also contributed to 3 more, which together make up the CROSS City system:

- [CROSS Contract](https://github.com/inesc-id/SureThing_CROSS_Data)
- [CROSS Server](https://github.com/inesc-id/CROSSCityCloud)
- [CROSS Gems-Payment Scanner](https://github.com/inesc-id/CROSSPaymentScanner)

## Steps to run the application

1. Create a Firebase project and add it to the Android app (you can follow the setup instructions [here](https://firebase.google.com/docs/android/setup#java) up to step 3 phase 1), export the [credentials](https://console.firebase.google.com/project/_/settings/serviceaccounts/adminsdk), put them in the server resources [folder](https://github.com/inesc-id/CROSSCityCloud/blob/main/CROSSCityServer/src/main/resources/firebase) and replace this [line](https://github.com/inesc-id/CROSSCityCloud/blob/main/CROSSCityServer/src/main/java/pt/ulisboa/tecnico/cross/utils/MessagingUtils.java#L34) with the credentials path name.
2. Run the [server](https://github.com/inesc-id/CROSSCityCloud/tree/main/CROSSCityServer).
3. Edit the IP of the `CROSS_SERVER_BASE_URL` [property](https://github.com/inesc-id/CROSSCityApp/blob/main/app/src/main/assets/CROSSCityApp.properties#L2) to that of your private network if you intend to run the application on a physical device or *10.0.2.2* if you intend to run it on an emulator.
4. Open the project in Android Studio and run the application on a device that has Google Play Store (required to enable location services) and that the API level is >= 26 (Android 8.0).

## Background

This mobile application is an extended re-implementation of the CROSS City app [prototype](https://github.com/inesc-id/CROSS-client) previously developed by [@gbl08ma](https://github.com/gbl08ma). This new version of the app was developed from scratch, because: the project dependencies were already quite outdated; the server communication API has been completely [rebuilt](https://github.com/inesc-id/SureThing_CROSS_Data) with a new messaging specification, [protocol-buffers](https://developers.google.com/protocol-buffers); the app logic could be simplified; and finally, for these reasons and the need to understand the source code in detail, we decided to perform an incremental development, studying the existing app source code as we were developing it.

## Author

| Name | University | More info |
|:-----|:-----------|:---------:|
| Ricardo Grade | Instituto Superior Técnico | [<img src="https://i.ibb.co/brG8fnX/mail-6.png" width="17">](mailto:ricardo.grade@tecnico.ulisboa.pt "ricardo.grade@tecnico.ulisboa.pt") [<img src="https://github.githubassets.com/favicon.ico" width="17">](https://github.com/RicardoGrade "RicardoGrade") [<img src="https://i.ibb.co/TvQPw7N/linkedin-logo.png" width="17">](https://www.linkedin.com/in/RicardoGrade "RicardoGrade") |
