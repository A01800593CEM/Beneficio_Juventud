import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import admin from "firebase-admin";
const serviceAccount = require("../beneficio-joven-firebase-adminsdk-fbsvc-076ac563fc.json")

@Injectable()
export class NotificationsService {
  
  

}


admin.initializeApp({
credential: admin.credential.cert(serviceAccount),
databaseURL: "https://beneficio-joven-default-rtdb.firebaseio.com"
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

    // Tema
      await admin.messaging().send({
        topic: 'TarjetaJoven',
        notification: {
          title: '¡Noticia del día!',
          body: '¡Mira lo nuevo en nuestras promociones!'
        },
        data: { // Opcional: datos personalizados
          key1: 'Ropa y accesorios para la familia',
          key2: 'value2'
        }
      });
}


//sendNotification().catch(console.error);
