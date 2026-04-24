package com.duoc.backend.service;

import com.duoc.backend.entity.Recipe;
import com.duoc.backend.entity.User;
import com.duoc.backend.repository.RecipeRepository;
import com.duoc.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Carga datos iniciales en la BD al arrancar la aplicación si las tablas están
 * vacías.
 * Se ejecuta una sola vez (idempotente): verifica count() antes de insertar.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

        private final UserRepository userRepository;
        private final RecipeRepository recipeRepository;
        private final PasswordEncoder passwordEncoder;

        @Value("${app.init.admin-password}")
        private String adminPassword;

        @Value("${app.init.chef-password}")
        private String chefPassword;

        @Value("${app.init.user-password}")
        private String userPassword;

        @Override
        public void run(String... args) {
                initUsers();
                initRecipes();
        }

        private void initUsers() {
                if (userRepository.count() > 0)
                        return;

                userRepository.saveAll(List.of(
                                new User(null, "admin", passwordEncoder.encode(adminPassword), "ROLE_ADMIN"),
                                new User(null, "chef", passwordEncoder.encode(chefPassword), "ROLE_USER"),
                                new User(null, "usuario", passwordEncoder.encode(userPassword), "ROLE_USER")));
                System.out.println(">>> Usuarios de prueba cargados.");
        }

        @Transactional
        public void initRecipes() {
                if (recipeRepository.count() > 0)
                        return;

                // ── 1. Pasta Carbonara ──────────────────────────────────────────────────
                Recipe carbonara = new Recipe();
                carbonara.setNombre("Pasta Carbonara");
                carbonara.setTipoCocina("Italiana");
                carbonara.setPaisOrigen("Italia");
                carbonara.setDificultad("Media");
                carbonara.setTiempoCoccion(30);
                carbonara.setDescripcionCorta("Pasta cremosa con guanciale, yemas de huevo y Pecorino Romano.");
                carbonara.setDescripcion(
                                "La auténtica Carbonara romana prescinde de la nata: la cremosidad proviene de la emulsión entre las yemas, el queso curado y el agua de cocción de la pasta. Un plato sencillo pero que exige técnica precisa para evitar que el huevo cuaje.");
                carbonara.setImagenUrl("https://images.unsplash.com/photo-1612874742237-6526221588e3?w=800");
                carbonara.setPopularidad(92);
                carbonara.setReciente(false);
                carbonara.setIngredientes(List.of(
                                "400 g de spaghetti",
                                "150 g de guanciale (o panceta)",
                                "4 yemas de huevo + 1 huevo entero",
                                "100 g de Pecorino Romano rallado",
                                "Pimienta negra recién molida",
                                "Sal gruesa para el agua"));
                carbonara.setInstrucciones(List.of(
                                "Cocinar el spaghetti en abundante agua con sal hasta que esté al dente.",
                                "Dorar el guanciale en trozos en una sartén sin aceite hasta que esté crujiente.",
                                "Batir las yemas con el huevo entero y el Pecorino, añadir pimienta generosa.",
                                "Retirar la sartén del fuego. Agregar la pasta escurrida y mezclar con el guanciale.",
                                "Añadir la mezcla de huevo poco a poco junto con un poco de agua de cocción, revolviendo rápido para obtener una crema sedosa.",
                                "Servir de inmediato con más Pecorino y pimienta al gusto."));

                // ── 2. Tacos al Pastor ─────────────────────────────────────────────────
                Recipe tacos = new Recipe();
                tacos.setNombre("Tacos al Pastor");
                tacos.setTipoCocina("Mexicana");
                tacos.setPaisOrigen("México");
                tacos.setDificultad("Media");
                tacos.setTiempoCoccion(60);
                tacos.setDescripcionCorta("Clásico taco mexicano con cerdo marinado en achiote y piña.");
                tacos.setDescripcion(
                                "Los tacos al pastor son uno de los iconos de la gastronomía mexicana. La carne de cerdo se marina en una mezcla de chiles y achiote, se cocina lentamente y se sirve con piña, cebolla y cilantro sobre tortilla de maíz.");
                tacos.setImagenUrl("https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=800");
                tacos.setPopularidad(95);
                tacos.setReciente(true);
                tacos.setIngredientes(List.of(
                                "800 g de lomo de cerdo en láminas",
                                "3 chiles guajillo desvenados",
                                "2 chiles ancho",
                                "3 cdas. de pasta de achiote",
                                "2 dientes de ajo",
                                "1/2 piña natural",
                                "Tortillas de maíz",
                                "Cebolla blanca y cilantro para servir",
                                "Salsa verde al gusto"));
                tacos.setInstrucciones(List.of(
                                "Remojar los chiles en agua caliente 20 minutos, luego licuar con el achiote, ajo y una taza del agua de remojo.",
                                "Marinar el cerdo con la mezcla al menos 2 horas (mejor de un día para otro).",
                                "Calentar una plancha o comal a fuego alto y cocinar la carne en tandas hasta dorar.",
                                "Picar la carne finamente con cuchillo o tijeras de cocina.",
                                "Asar rodajas de piña en el mismo comal.",
                                "Servir en tortillas calientes con carne, piña, cebolla, cilantro y salsa."));

                // ── 3. Sushi de Salmón ────────────────────────────────────────────────
                Recipe sushi = new Recipe();
                sushi.setNombre("Sushi de Salmón");
                sushi.setTipoCocina("Japonesa");
                sushi.setPaisOrigen("Japón");
                sushi.setDificultad("Dificil");
                sushi.setTiempoCoccion(90);
                sushi.setDescripcionCorta("Rollos de sushi frescos con salmón, palta y pepino.");
                sushi.setDescripcion(
                                "El sushi casero requiere paciencia, pero el resultado es espectacular. La clave está en el arroz perfectamente sazonado y en el corte del salmón fresco. Esta receta abarca rolls tipo uramaki (arroz por fuera) y nigiri básico.");
                sushi.setImagenUrl("https://images.unsplash.com/photo-1553621042-f6e147245754?w=800");
                sushi.setPopularidad(88);
                sushi.setReciente(false);
                sushi.setIngredientes(List.of(
                                "300 g de arroz para sushi",
                                "60 ml de vinagre de arroz",
                                "2 cdas. de azúcar",
                                "1 cdita. de sal",
                                "4 hojas de alga nori",
                                "200 g de salmón fresco grado sushi",
                                "1 palta (aguacate)",
                                "1 pepino japonés",
                                "Salsa de soya, wasabi y jengibre encurtido para servir"));
                sushi.setInstrucciones(List.of(
                                "Lavar el arroz varias veces hasta que el agua salga clara; cocinar con proporción 1:1,1 de agua.",
                                "Disolver el azúcar y la sal en el vinagre caliente; mezclar con el arroz ya cocido y enfriar a temperatura ambiente.",
                                "Cortar el salmón, el pepino y el aguacate en tiras uniformes.",
                                "Colocar una hoja de nori sobre el makisu (estera de bambú), cubrir con arroz dejando 2 cm libres en el extremo.",
                                "Añadir el relleno en el centro, enrollar con firmeza y sellar con agua el borde libre.",
                                "Cortar cada rollo en 8 piezas con cuchillo mojado y servir con soya, wasabi y jengibre."));

                // ── 4. Paella Valenciana ──────────────────────────────────────────────
                Recipe paella = new Recipe();
                paella.setNombre("Paella Valenciana");
                paella.setTipoCocina("Española");
                paella.setPaisOrigen("España");
                paella.setDificultad("Dificil");
                paella.setTiempoCoccion(90);
                paella.setDescripcionCorta("El arroz más famoso de España con pollo, conejo y judías verdes.");
                paella.setDescripcion(
                                "La paella valenciana auténtica lleva pollo, conejo, judías verdes (bajoqueta) y garrofó. La clave es el socarrat, la capa caramelizada de arroz que se forma en el fondo de la paellera. Se cocina a fuego de leña en Valencia, pero una cocina de gas sirve perfectamente.");
                paella.setImagenUrl("https://images.unsplash.com/photo-1534080564583-6be75777b70a?w=800");
                paella.setPopularidad(85);
                paella.setReciente(true);
                paella.setIngredientes(List.of(
                                "400 g de arroz bomba",
                                "500 g de pollo troceado",
                                "300 g de conejo troceado",
                                "200 g de judías verdes planas",
                                "150 g de garrofó (alubia grande) cocida",
                                "2 tomates rallados",
                                "1 cdita. de pimentón dulce",
                                "Hebras de azafrán",
                                "1,2 L de caldo de pollo caliente",
                                "Aceite de oliva virgen extra y sal"));
                paella.setInstrucciones(List.of(
                                "Calentar aceite en la paellera y sofreír el pollo y el conejo salpimentados hasta dorar.",
                                "Agregar las judías y el garrofó; sofreír 5 minutos. Añadir el tomate rallado y cocinar hasta que pierda el agua.",
                                "Incorporar el pimentón, remover rápido (no se queme) y verter el caldo caliente con el azafrán.",
                                "Llevar a ebullición, ajustar sal y añadir el arroz distribuyéndolo uniformemente.",
                                "Cocinar a fuego fuerte 8 minutos, luego reducir al mínimo 10 minutos más sin remover.",
                                "Subir el fuego 1-2 minutos al final para obtener el socarrat; reposar 5 minutos tapado con papel de periódico."));

                // ── 5. Guacamole Clásico ──────────────────────────────────────────────
                Recipe guacamole = new Recipe();
                guacamole.setNombre("Guacamole Clásico");
                guacamole.setTipoCocina("Mexicana");
                guacamole.setPaisOrigen("México");
                guacamole.setDificultad("Facil");
                guacamole.setTiempoCoccion(10);
                guacamole.setDescripcionCorta(
                                "Dip de palta fresca con limón, jalapeño y cilantro. Listo en 10 minutos.");
                guacamole.setDescripcion(
                                "El guacamole perfecto no necesita más que aguacates bien maduros y pocos ingredientes frescos. La textura ideal es rústica, no completamente lisa. Perfecto como dip de totopos o acompañamiento de cualquier plato mexicano.");
                guacamole.setImagenUrl("https://images.unsplash.com/photo-1548943487-a2e4e43b4853?w=800");
                guacamole.setPopularidad(90);
                guacamole.setReciente(true);
                guacamole.setIngredientes(List.of(
                                "3 aguacates maduros",
                                "Jugo de 2 limones",
                                "1/4 de cebolla blanca finamente picada",
                                "1 jalapeño sin semillas picado",
                                "1/4 de taza de cilantro picado",
                                "1 tomate picado sin semillas",
                                "Sal al gusto"));
                guacamole.setInstrucciones(List.of(
                                "Cortar los aguacates por la mitad, retirar el hueso y sacar la pulpa con una cuchara.",
                                "Machacar la pulpa en un molcajete o bol con un tenedor; dejar trozos para obtener textura.",
                                "Añadir el jugo de limón de inmediato para evitar la oxidación.",
                                "Incorporar la cebolla, jalapeño, cilantro y tomate; mezclar suavemente.",
                                "Sazonar con sal, probar y ajustar ácidez. Servir de inmediato con totopos."));

                // ── 6. Ceviche Peruano ────────────────────────────────────────────────
                Recipe ceviche = new Recipe();
                ceviche.setNombre("Ceviche Peruano");
                ceviche.setTipoCocina("Peruana");
                ceviche.setPaisOrigen("Perú");
                ceviche.setDificultad("Media");
                ceviche.setTiempoCoccion(20);
                ceviche.setDescripcionCorta("Pescado blanco fresco marinado en leche de tigre con ají amarillo.");
                ceviche.setDescripcion(
                                "El ceviche peruano es el plato nacional del Perú y patrimonio cultural. A diferencia de otros ceviches latinoamericanos, el peruano usa ají amarillo y se marina por solo unos minutos, consiguiendo una textura firme pero tierna. La leche de tigre que queda es un elixir que muchos beben directamente.");
                ceviche.setImagenUrl("https://images.unsplash.com/photo-1535399831218-d5bd36d1a6b3?w=800");
                ceviche.setPopularidad(87);
                ceviche.setReciente(false);
                ceviche.setIngredientes(List.of(
                                "500 g de corvina o lenguado fresco",
                                "Jugo de 10 limones peruanos (o lima)",
                                "1 cebolla morada en juliana fina",
                                "2 ajíes amarillos sin venas ni semillas",
                                "1 diente de ajo",
                                "1 cm de jengibre fresco",
                                "Cilantro fresco al gusto",
                                "Sal y pimienta",
                                "Choclo cocido y camote amarillo para acompañar"));
                ceviche.setInstrucciones(List.of(
                                "Cortar el pescado en cubos de 2 cm y refrigerar mientras se prepara la leche de tigre.",
                                "Licuar el jugo de limón con el ají amarillo, ajo, jengibre y una pizca de sal; colar.",
                                "Lavar la cebolla en juliana con agua fría y sal; escurrir.",
                                "Sazonar el pescado con sal y pimienta, verter la leche de tigre y mezclar.",
                                "Añadir la cebolla y el cilantro; dejar reposar 3-5 minutos (no más para que no se sobre-cocine).",
                                "Servir de inmediato en bowl frío con choclo y camote sancochado."));

                recipeRepository.saveAll(List.of(carbonara, tacos, sushi, paella, guacamole, ceviche));
                System.out.println(">>> 6 recetas iniciales cargadas.");
        }
}
