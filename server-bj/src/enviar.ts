import admin from "firebase-admin";
const serviceAccount = require("../beneficio-joven-firebase-adminsdk-fbsvc-0852e25dec.json");
admin.initializeApp({
credential: admin.credential.cert(serviceAccount),
databaseURL: "https://beneficio-joven-default-rtdb.firebaseio.com"
});
const token = "d6pkRCOgTVCX4CezhHXNvh:APA91bFtNzHwDJOhmWdU6ZaTiyC5bce3H00knep7lcGgn5qTprmBtsGJamSGwmQmLWuq4utjMaEwFyNmimJuEKMCfoy36FIaRbJE7kEx4z-GZhMgndcJVxg";

export async function sendNotification() {
    await admin.messaging().send({
        token: token,
        notification: {
            title: '¡Hola!',
            body: 'Esta es una notificación de prueba desde Javascript.'
        },
        data: { // Opcional: datos personalizados
            key1: 'value1',
            key2: 'value2'
        }
    });
    console.log("Termina el envío");
}

//sendNotification().catch(console.error);