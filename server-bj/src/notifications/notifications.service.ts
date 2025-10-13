import { Inject, Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import admin from "firebase-admin";


const token = "d6pkRCOgTVCX4CezhHXNvh:APA91bFtNzHwDJOhmWdU6ZaTiyC5bce3H00knep7lcGgn5qTprmBtsGJamSGwmQmLWuq4utjMaEwFyNmimJuEKMCfoy36FIaRbJE7kEx4z-GZhMgndcJVxg";

@Injectable()
export class NotificationsService {
  constructor(@Inject('FIREBASE_ADMIN') private admin: admin.app.App) {}

  async sendNotification() {
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

}

//sendNotification().catch(console.error);
