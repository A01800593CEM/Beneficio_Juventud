package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import mx.itesm.beneficiojuventud.components.BJTopHeader
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme

/**
 * Data class para contener una sección de los Términos y Condiciones.
 * @param title El título de la sección (ej., "1. NUESTROS SERVICIOS").
 * @param content El contenido del párrafo de la sección.
 */
data class TermSection(val title: String, val content: String)

/**
 * Un Composable que muestra una pantalla de Términos y Condiciones.
 * Cuenta con una barra de aplicación superior y un cuerpo de texto desplazable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndConditionsScreen(nav: NavHostController, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = {
            BJTopHeader(
                title = "Términos y Condiciones",
                nav = nav,
                showNotificationsIcon = false
            )
        }
    ) { innerPadding ->
        val introText = "Nosotros somos el Gobierno de Atizapán (\"Gobierno\", \"nosotros\", \"nos\", \"nuestro\"), un organismo gubernamental con domicilio en Blvd. Adolfo López Mateos 91, El Potrero, Méx., Atizapán de Zaragoza, Estado de México 52987.\n\nOperamos la aplicación móvil Beneficio Joven (la \"App\"), así como cualquier otro producto y servicio relacionado que se refiera o enlace a estos términos legales (los \"Términos Legales\") (colectivamente, los \"Servicios\").\n\nOfrecemos un programa de cupones para el gobierno de Atizapán con el fin de impulsar la economía local, proporcionando a los ciudadanos de Atizapán descuentos o códigos promocionales (códigos QR) para su uso en diversos campos como alimentación, vestimenta, entretenimiento y más. Los colaboradores (las tiendas) pueden consultar estadísticas sobre sus promociones para generar más y mejores interacciones.\n\nPuede contactarnos por correo electrónico a [contact@example.com] o por correo postal a Blvd. Adolfo López Mateos 91, El Potrero, Méx., Atizapán de Zaragoza, Estado de México 52987.\n\nEstos Términos Legales constituyen un acuerdo legalmente vinculante entre usted, ya sea personalmente o en nombre de una entidad (\"usted\"), y el Gobierno de Atizapán, en relación con su acceso y uso de los Servicios. Usted acepta que, al acceder a los Servicios, ha leído, entendido y aceptado regirse por todos estos Términos Legales. SI NO ESTÁ DE ACUERDO CON TODOS ESTOS TÉRMINOS LEGALES, ENTONCES SE LE PROHIBE EXPRESAMENTE EL USO DE LOS SERVICIOS Y DEBE DEJAR DE UTILIZARLOS INMEDIATAMENTE.\n\nLe recomendamos que imprima una copia de estos Términos Legales para sus registros."
        val termsSections = getAppTerms()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding), // Aplicar padding del Scaffold
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp)
        ) {
            // Título Principal
            item {
                Text(
                    text = "ACUERDO DE NUESTROS TÉRMINOS LEGALES",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF616161)
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            // Fecha de última actualización
            item {
                Text(
                    text = "Última actualización: 18 de octubre de 2025",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            // Texto introductorio
            item {
                Text(
                    text = introText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textAlign = TextAlign.Justify,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFAFAFAF)
                    ),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
            // Muestra cada sección con título y contenido
            items(termsSections) { section ->
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF616161)
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = section.content,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textAlign = TextAlign.Justify,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFAFAFAF)
                        ),
                    )
                }
            }
        }
    }
}


/**
 * Obtiene la lista de secciones de términos y condiciones.
 * @return Una lista de objetos [TermSection].
 */
private fun getAppTerms(): List<TermSection> {
    return listOf(
        TermSection(
            "1. NUESTROS SERVICIOS",
            "La información proporcionada al usar los Servicios no está destinada a ser distribuida o utilizada por ninguna persona o entidad en ninguna jurisdicción o país donde dicha distribución o uso sea contrario a la ley o regulación, o que nos sujete a algún requisito de registro dentro de dicha jurisdicción o país. Aquellas personas que elijan acceder a los Servicios desde otras ubicaciones lo hacen por iniciativa propia y son las únicas responsables del cumplimiento de las leyes locales, si y en la medida en que las leyes locales sean aplicables."
        ),
        TermSection(
            "2. DERECHOS DE PROPIEDAD INTELECTUAL",
            "Somos los propietarios o licenciatarios de todos los derechos de propiedad intelectual de nuestros Servicios, incluyendo todo el código fuente, bases de datos, funcionalidad, software, diseños de sitios web, audio, video, texto, fotografías y gráficos en los Servicios (colectivamente, el \"Contenido\"), así como las marcas comerciales, marcas de servicio y logotipos contenidos en ellos (las \"Marcas\"). Nuestro Contenido y Marcas están protegidos por las leyes de derechos de autor y marcas registradas de México y tratados internacionales. El Contenido y las Marcas se proporcionan en o a través de los Servicios \"TAL CUAL\" para su información y uso personal, no comercial o empresarial interno únicamente."
        ),
        TermSection(
            "3. DECLARACIONES DEL USUARIO",
            "Al utilizar los Servicios, usted declara y garantiza que: (1) toda la información de registro que envíe será verdadera, precisa, actual y completa; (2) mantendrá la exactitud de dicha información y la actualizará puntualmente según sea necesario; (3) tiene la capacidad legal y acepta cumplir con estos Términos Legales; (4) no es menor de 13 años; (5) no es mayor de 29 años de edad; (6) no es menor de edad en la jurisdicción en la que reside, o si es menor de edad, ha recibido el permiso de sus padres para usar los Servicios; (7) no accederá a los Servicios a través de medios automatizados o no humanos, ya sea a través de un bot, script o de otra manera; (8) no utilizará los Servicios para ningún propósito ilegal o no autorizado; y (9) su uso de los Servicios no violará ninguna ley o regulación aplicable."
        ),
        TermSection(
            "4. REGISTRO DE USUARIO",
            "Es posible que se le solicite que se registre para utilizar los Servicios. Usted se compromete a mantener la confidencialidad de su contraseña y será responsable de todo uso de su cuenta y contraseña. Nos reservamos el derecho de eliminar, reclamar o cambiar un nombre de usuario que seleccionemos si determinamos, a nuestra entera discreción, que dicho nombre de usuario es inapropiado, obsceno u objetable de alguna otra manera."
        ),
        TermSection(
            "5. ACTIVIDADES PROHIBIDAS",
            "Usted no puede acceder ni utilizar los Servicios para ningún otro propósito que no sea aquel para el cual los ponemos a disposición. Los Servicios no pueden ser utilizados en conexión con ningún esfuerzo comercial, excepto aquellos que están específicamente respaldados o aprobados por nosotros."
        ),
        TermSection(
            "6. VALIDACIÓN Y USO DE CUPONES",
            "Al utilizar un cupón o descuento a través de los Servicios en un negocio colaborador, usted acepta y reconoce lo siguiente:\n\n• El personal del negocio colaborador tiene el derecho de solicitarle una identificación oficial vigente (por ejemplo, INE, pasaporte, licencia de conducir) para verificar que la identidad del portador coincide con los datos asociados al cupón o a la cuenta de la App.\n\n• En caso de que un usuario intente utilizar un cupón de manera fraudulenta, presente información falsa, intente redimir un cupón ya utilizado o se niegue a verificar su identidad cuando se le solicite, el negocio colaborador se reserva el pleno derecho de negar la validez del cupón y/o la prestación del servicio o producto asociado a la promoción."
        ),
        TermSection(
            "7. CONTRIBUCIONES GENERADAS POR EL USUARIO",
            "Los Servicios no ofrecen a los usuarios la posibilidad de enviar o publicar contenido."
        ),
        TermSection(
            "8. LICENCIA DE CONTRIBUCIÓN",
            "Usted y los Servicios acuerdan que podemos acceder, almacenar, procesar y utilizar cualquier información y datos personales que usted proporcione siguiendo los términos de la Política de Privacidad y sus elecciones (incluidas las configuraciones)."
        ),
        TermSection(
            "9. LICENCIA DE LA APLICACIÓN MÓVIL",
            "Si accede a los Servicios a través de la App, le otorgamos un derecho revocable, no exclusivo, intransferible y limitado para instalar y usar la App en dispositivos electrónicos inalámbricos de su propiedad o controlados por usted, y para acceder y usar la App en dichos dispositivos estrictamente de acuerdo con los términos y condiciones de esta licencia de aplicación móvil contenidos en estos Términos Legales."
        ),
        TermSection(
            "10. GESTIÓN DE LOS SERVICIOS",
            "Nos reservamos el derecho, pero no la obligación, de: (1) monitorear los Servicios en busca de violaciones de estos Términos Legales; (2) tomar las acciones legales apropiadas contra cualquier persona que, a nuestra entera discreción, viole la ley o estos Términos Legales; (3) a nuestra entera discreción y sin limitación, rechazar, restringir el acceso, limitar la disponibilidad o deshabilitar (en la medida tecnológicamente factible) cualquiera de sus Contribuciones o cualquier parte de ellas; (4) gestionar los Servicios de una manera diseñada para proteger nuestros derechos y propiedad y para facilitar el funcionamiento adecuado de los Servicios."
        ),
        TermSection(
            "11. POLÍTICA DE PRIVACIDAD",
            "Nos preocupamos por la privacidad y la seguridad de los datos. Al utilizar los Servicios, usted acepta estar sujeto a nuestra Política de Privacidad publicada en los Servicios, la cual se incorpora a estos Términos Legales."
        ),
        TermSection(
            "12. VIGENCIA Y TERMINACIÓN",
            "Estos Términos Legales permanecerán en pleno vigor y efecto mientras usted utilice los Servicios. SIN LIMITAR NINGUNA OTRA DISPOSICIÓN DE ESTOS TÉRMINOS LEGALES, NOS RESERVAMOS EL DERECHO DE, A NUESTRA ENTERA DISCRECIÓN Y SIN PREVIO AVISO NI RESPONSABILIDAD, DENEGAR EL ACCESO Y USO DE LOS SERVICIOS (INCLUIDO EL BLOQUEO DE CIERTAS DIRECCIONES IP) A CUALQUIER PERSONA POR CUALQUIER MOTIVO O SIN MOTIVO ALGUNO."
        ),
        TermSection(
            "13. MODIFICACIONES E INTERRUPCIONES",
            "Nos reservamos el derecho de cambiar, modificar o eliminar el contenido de los Servicios en cualquier momento o por cualquier motivo a nuestra entera discreción y sin previo aviso. Sin embargo, no tenemos la obligación de actualizar ninguna información en nuestros Servicios. No seremos responsables ante usted ni ante ningún tercero por ninguna modificación, cambio de precio, suspensión o interrupción de los Servicios."
        ),
        TermSection(
            "14. LEY APLICABLE Y JURISDICCIÓN",
            "Estos Términos Legales se regirán e interpretarán de acuerdo con las leyes federales de los Estados Unidos Mexicanos. Usted y el Gobierno de Atizapán de Zaragoza consienten irrevocablemente que los tribunales competentes en el Municipio de Atizapán de Zaragoza, Estado de México, tendrán jurisdicción exclusiva para resolver cualquier disputa que pueda surgir en conexión con estos Términos Legales."
        ),
        TermSection(
            "15. CORRECCIONES",
            "Puede haber información en los Servicios que contenga errores tipográficos, inexactitudes u omisiones. Nos reservamos el derecho de corregir cualquier error, inexactitud u omisión y de cambiar o actualizar la información en los Servicios en cualquier momento, sin previo aviso."
        ),
        TermSection(
            "16. DESCARGO DE RESPONSABILIDAD",
            "LOS SERVICIOS SE PROPORCIONAN \"TAL CUAL\" Y \"SEGÚN DISPONIBILIDAD\". USTED ACEPTA QUE SU USO DE LOS SERVICIOS SERÁ BAJO SU PROPIO RIESGO. EN LA MÁXIMA MEDIDA PERMITIDA POR LA LEY, RENUNCIAMOS A TODAS LAS GARANTÍAS, EXPRESAS O IMPLÍCITAS, EN RELACIÓN CON LOS SERVICIOS Y SU USO DE LOS MISMOS."
        ),
        TermSection(
            "17. LIMITACIONES DE RESPONSABILIDAD",
            "EN NINGÚN CASO NOSOTROS O NUESTROS DIRECTORES, EMPLEADOS O AGENTES SEREMOS RESPONSABLES ANTE USTED O CUALQUIER TERCERO POR DAÑOS DIRECTOS, INDIRECTOS, CONSECUENTES, EJEMPLARES, INCIDENTALES, ESPECIALES O PUNITIVOS, INCLUIDA LA PÉRDIDA DE BENEFICIOS, PÉRDIDA DE INGRESOS, PÉRDIDA DE DATOS U OTROS DAÑOS DERIVADOS DE SU USO DE LOS SERVICIOS, INCLUSO SI HEMOS SIDO ADVERTIDOS DE LA POSIBILIDAD DE DICHOS DAÑOS."
        ),
        TermSection(
            "18. INDEMNIZACIÓN",
            "Usted acepta defender, indemnizar y mantenernos indemnes, incluidas nuestras subsidiarias, filiales y todos nuestros respectivos funcionarios, agentes, socios y empleados, de y contra cualquier pérdida, daño, responsabilidad, reclamación o demanda, incluidos los honorarios razonables de abogados y gastos, realizados por cualquier tercero debido a o que surjan de: (1) el uso de los Servicios; (2) el incumplimiento de estos Términos Legales; (3) cualquier incumplimiento de sus declaraciones y garantías establecidas en estos Términos Legales; (4) su violación de los derechos de un tercero, incluidos, entre otros, los derechos de propiedad intelectual."
        ),
        TermSection(
            "19. DATOS DEL USUARIO",
            "Mantendremos ciertos datos que usted transmita a los Servicios con el fin de gestionar el rendimiento de los mismos, así como datos relacionados con su uso de los Servicios. Aunque realizamos copias de seguridad rutinarias y regulares de los datos, usted es el único responsable de todos los datos que transmita o que se relacionen con cualquier actividad que haya emprendido utilizando los Servicios."
        ),
        TermSection(
            "20. COMUNICACIONES ELECTRÓNICAS, TRANSACCIONES Y FIRMAS",
            "Visitar los Servicios, enviarnos correos electrónicos y completar formularios en línea constituyen comunicaciones electrónicas. Usted consiente en recibir comunicaciones electrónicas y acepta que todos los acuerdos, avisos, divulgaciones y otras comunicaciones que le proporcionamos electrónicamente, por correo electrónico y en los Servicios, satisfacen cualquier requisito legal de que dicha comunicación sea por escrito."
        ),
        TermSection(
            "21. MISCELÁNEOS",
            "Estos Términos Legales y cualquier política o regla operativa publicada por nosotros en los Servicios constituyen el acuerdo y entendimiento completo entre usted y nosotros. Nuestra incapacidad para ejercer o hacer cumplir cualquier derecho o disposición de estos Términos Legales no operará como una renuncia a dicho derecho o disposición."
        ),
        TermSection(
            "22. CONTACTO",
            "Para resolver una queja sobre los Servicios o para recibir más información sobre el uso de los mismos, contáctenos en:\n\nGobierno de Atizapán\nBlvd. Adolfo López Mateos 91, El Potrero, Méx.\nAtizapán de Zaragoza, Estado de México 52987\nMéxico\nTeléfono: [5536222800]\n[soporte@atizapan.gob.mx]"
        )
    )
}

/**
 * Función de previsualización para la pantalla TermsAndConditionsScreen.
 * Permite ver la UI en el panel de vista previa de Android Studio.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun TermsAndConditionsScreenPreview() {
    BeneficioJuventudTheme {
        val nav = rememberNavController()
        TermsAndConditionsScreen(nav = nav)
    }
}