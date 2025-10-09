import admin from "firebase-admin";
const serviceAccount = require("../applogin-31ac0-firebase-adminsdk-fbsvc-d677e8f97e.json");
admin.initializeApp({
credential: admin.credential.cert(serviceAccount),
databaseURL: "https://applogin-31ac0-default-rtdb.firebaseio.com"
});
const token = "cMZ4pfTVROyYYzP281SGjk:APA91bGJwFLHKiyFDfT-wEBM8Ycvyy8tY-HtxFGSsPov7Ej6Xm-5j8gRcOirXBGh6NdOpG7DWgu8YMEFLOL2RVA_Gl1wMPF25sGZH4XeS3kNxp-NEpvpXyQ";

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