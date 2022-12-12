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

## Publication

Grade, R.; Eisa, S. & Pardal, M. L.  
_Bluetooth Peer-to-Peer Location Certification with a Gamified Mobile Application_  
21st IEEE International Symposium on Network Computing and Applications (NCA), 2022  

```bibtex
@InProceedings{Grade_2022_NCA_P2P-Witnessing,
  author    = {Ricardo Grade and Samih Eisa and Miguel L. Pardal},
  booktitle = {21st IEEE International Symposium on Network Computing and Applications (NCA)},
  title     = {{Bluetooth Peer-to-Peer Location Certification with a Gamified Mobile Application}},
  year      = {2022},
  month     = dec,
  abstract  = {Nowadays, tourists turn to digital platforms to discover new places to explore. CROSS City is a smart tourism mobile application that enhances the user experience of tourists visiting points of interest in a route by rewarding them in the end, if they actually visited all locations. From a technical standpoint, the user location is certified resorting to strategies that take advantage of both the diversity of the existing Wi-Fi network infrastructure throughout the city, as well as the presence of other users at the same site using Bluetooth. This work developed a new, peer-to-peer location certification strategy and added gamification elements to encourage users to keep the wireless radios turned on and use the app more. This work was evaluated both in laboratory experiments and with users in a real-world scenario which demonstrated that the new Bluetooth peer-based strategy is both feasible and resistant to collusion attacks.},
  keywords  = {Location Certification, Location Spoofing Prevention, Peer-to-Peer Communication, Bluetooth, Internet of Things, Gamification, User Experience},
}
```
