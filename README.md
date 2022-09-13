# HAR_app
An Android App named PDIoT which implements real time human activity recognition via 
blue tooth connectable RESpeck sensor.

This repo contains:

`HAR_pdiot_app`: the APP itself scripted in Java and Kotlin.


`raw_data`: a [public HAR dataset](https://github.com/specknet/pdiot-data/tree/master/2021) collected from 49 subjects 
with RESpeck sensor. Each subject records an 30s data of each activity class, stored in `Respeck_UUN_class_date_time.xlsx` file.

`classifier for 5 classes`: a tutorial of applying CNN and GRU on the above dataset in Tensorflow. Learned tf-lite model will
be deployed on App to achieve online classification. The best model can get will have accuracy of 95% on testing set.

## Installation

Open `HAR_pdiot_app` via Android Studio, open developer mode of target Android device, find the device in 
Android Studio and press run.

## App design

Below shows the App interface. From left to right are main page, RESpeck sensor connection page 
and prediction page. The App has a extra sedentary reminder functionality.
![alt text](./figs/app_design.png?raw=true)


## Online classification

This small demo shows the App classification result when subject is falling.
![Alt Text](./figs/fallings.gif)
