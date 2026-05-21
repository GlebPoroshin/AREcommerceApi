package com.poroshin.rut.ar.api.config

import com.poroshin.rut.ar.api.entity.ArMetadata
import com.poroshin.rut.ar.api.entity.ProductDocument
import com.poroshin.rut.ar.api.model.ArPlacement
import com.poroshin.rut.ar.api.model.ArType
import com.poroshin.rut.ar.api.repository.ProductRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DataInitializer {

    @Bean
    fun seedProducts(
        productRepository: ProductRepository,
        yandexS3Properties: YandexS3Properties,
    ): CommandLineRunner = CommandLineRunner {
        fun imageUrl(sku: Long): String =
            "${yandexS3Properties.endpoint}/${yandexS3Properties.buckets.images}/$sku.png"

        fun arMetadata(
            modelBaseName: String,
            width: Float,
            height: Float,
            depth: Float,
        ): ArMetadata = ArMetadata(
            version = 1,
            arType = ArType.OBJECT,
            placement = ArPlacement.ANY_HORIZONTAL,
            arResourceUrlAndroid =
                "${yandexS3Properties.endpoint}/${yandexS3Properties.buckets.models}/$modelBaseName.glb",
            arResourceUrlIos =
                "${yandexS3Properties.endpoint}/${yandexS3Properties.buckets.models}/$modelBaseName.usdz",
            width = width,
            height = height,
            depth = depth,
        )

        val products = listOf(
            ProductDocument(
                sku = 1000L,
                name = "Шезлонг складной Riva",
                description = "Деревянный складной шезлонг с парусиновым полотном. Лёгкий, помещается в багажник, не боится переездов на дачу. Каркас из массива бука, ткань — плотный хлопок цвета слоновой кости.",
                price = "8 990",
                imageUrl = imageUrl(1000L),
                oldPrice = "12 990",
                discount = 31,
                rate = 4.7,
                images = listOf(imageUrl(1000L)),
                rating = 4.7,
                characteristics = mapOf(
                    "Материал каркаса" to "массив бука",
                    "Материал полотна" to "хлопковая парусина",
                    "Цвет" to "натуральный / слоновая кость",
                    "Складной" to "да",
                    "Максимальная нагрузка" to "120 кг",
                    "Страна производства" to "Россия",
                ),
                stock = 47,
                deliveryInfo = "Доставка завтра, бесплатно от 5 000 ₽",
                arMetadata = arMetadata("01_deck_chair", 998f, 1069f, 668f),
            ),
            ProductDocument(
                sku = 1001L,
                name = "Стул обеденный Cambridge",
                description = "Классический обеденный стул с высокой спинкой и каретной стяжкой. Обивка из тёмно-коричневой искусственной кожи, чёрные деревянные ножки. Подходит к кухонной группе и кабинету.",
                price = "14 490",
                imageUrl = imageUrl(1001L),
                rate = 4.5,
                images = listOf(imageUrl(1001L)),
                rating = 4.5,
                characteristics = mapOf(
                    "Материал обивки" to "экокожа",
                    "Материал ножек" to "массив берёзы, окрашен в чёрный",
                    "Цвет" to "тёмно-коричневый",
                    "Декор" to "каретная стяжка (capitonné)",
                    "Максимальная нагрузка" to "110 кг",
                    "В комплекте" to "1 шт.",
                ),
                stock = 23,
                deliveryInfo = "Доставка послезавтра",
                arMetadata = arMetadata("02_dining_chair", 434f, 973f, 576f),
            ),
            ProductDocument(
                sku = 1002L,
                name = "Стеллаж-комод Nord",
                description = "Гибрид комода и стеллажа: три выдвижных ящика снизу для вещей и две открытые полки сверху для книг или декора. Каркас из чёрного металла, фасады и полки — массив ясеня. Подойдёт в прихожую, спальню или мастер-бедрум.",
                price = "32 990",
                imageUrl = imageUrl(1002L),
                oldPrice = "39 990",
                discount = 18,
                rate = 4.6,
                images = listOf(imageUrl(1002L)),
                rating = 4.6,
                characteristics = mapOf(
                    "Материал каркаса" to "сталь с порошковой окраской",
                    "Материал фасадов и полок" to "массив ясеня",
                    "Цвет" to "светлое дерево / чёрный",
                    "Количество ящиков" to "3",
                    "Количество открытых полок" to "2",
                    "Стиль" to "лофт / скандинавский",
                ),
                stock = 8,
                deliveryInfo = "Доставка 3–5 дней",
                arMetadata = arMetadata("03_drawer_cabinet", 1141f, 1881f, 488f),
            ),
            ProductDocument(
                sku = 1003L,
                name = "Журнальный стол Aether",
                description = "Дизайнерский журнальный стол: тёмный квадратный корпус и скульптурная медная проволочная вставка сверху. Акцентная вещь для гостиной — не для рабочих обедов, а для «поставить чашку и журнал».",
                price = "49 900",
                imageUrl = imageUrl(1003L),
                rate = 4.3,
                images = listOf(imageUrl(1003L)),
                rating = 4.3,
                characteristics = mapOf(
                    "Материал корпуса" to "МДФ с матовым покрытием",
                    "Материал декоративной вставки" to "латунь",
                    "Цвет" to "чёрный / медь",
                    "Стиль" to "дизайнерский / арт-деко",
                    "Форма" to "квадратная",
                ),
                stock = 4,
                deliveryInfo = "Доставка 7–10 дней",
                arMetadata = arMetadata("04_coffee_table_art", 1199f, 369f, 1200f),
            ),
            ProductDocument(
                sku = 1004L,
                name = "Стеллаж модульный Kvadrat 3×4",
                description = "Открытый стеллаж 3×4 ячейки — 12 одинаковых секций. В нижний ряд можно докупить ящики или коробки. Делит комнату, выдерживает книги, корзины и коллекции виниловых пластинок.",
                price = "21 990",
                imageUrl = imageUrl(1004L),
                rate = 4.8,
                images = listOf(imageUrl(1004L)),
                rating = 4.8,
                characteristics = mapOf(
                    "Материал" to "ЛДСП с текстурой дуб сонома",
                    "Цвет" to "натуральный дуб",
                    "Количество ячеек" to "12",
                    "Конфигурация" to "3 × 4",
                    "Нагрузка на полку" to "до 13 кг",
                    "В комплекте" to "крепёж к стене",
                ),
                stock = 31,
                deliveryInfo = "Доставка 2–3 дня",
                arMetadata = arMetadata("05_display_shelf", 372f, 1556f, 1078f),
            ),
            ProductDocument(
                sku = 1005L,
                name = "Журнальный столик Wave",
                description = "Круглый журнальный стол с белой мраморной столешницей и тремя гнутыми металлическими ножками. Лёгкий силуэт — не загромождает маленькую гостиную, но держит чайный сервиз и стопку книг.",
                price = "27 490",
                imageUrl = imageUrl(1005L),
                oldPrice = "32 990",
                discount = 17,
                rate = 4.6,
                images = listOf(imageUrl(1005L)),
                rating = 4.6,
                characteristics = mapOf(
                    "Материал столешницы" to "натуральный мрамор Carrara",
                    "Материал ножек" to "сталь, порошковая окраска",
                    "Цвет" to "белый / чёрный",
                    "Форма" to "круглая",
                    "Диаметр" to "1300 мм",
                    "Количество ножек" to "3",
                ),
                stock = 12,
                deliveryInfo = "Доставка 3–5 дней",
                arMetadata = arMetadata("06_coffee_table_round", 1301f, 491f, 1301f),
            ),
            ProductDocument(
                sku = 1006L,
                name = "Кресло Oslo",
                description = "Кресло в стиле mid-century: деревянный каркас из массива дуба и две съёмные подушки из натуральной кожи. Сиденье и спинку можно перетянуть отдельно, когда обивка устанет.",
                price = "42 990",
                imageUrl = imageUrl(1006L),
                rate = 4.9,
                images = listOf(imageUrl(1006L)),
                rating = 4.9,
                characteristics = mapOf(
                    "Материал каркаса" to "массив дуба",
                    "Материал обивки" to "натуральная кожа",
                    "Цвет обивки" to "чёрный",
                    "Цвет каркаса" to "светлый дуб",
                    "Стиль" to "mid-century modern",
                    "Подушки" to "съёмные, 2 шт.",
                ),
                stock = 6,
                deliveryInfo = "Доставка 5–7 дней",
                arMetadata = arMetadata("07_armchair", 820f, 1023f, 987f),
            ),
            ProductDocument(
                sku = 1007L,
                name = "Балконный набор Bistro: стол + 2 стула",
                description = "Складной набор из стола и двух стульев для балкона, террасы или маленькой кухни. Деревянные ламели из тика на металлическом каркасе. На зиму всё складывается и убирается в шкаф.",
                price = "19 990",
                imageUrl = imageUrl(1007L),
                oldPrice = "24 990",
                discount = 20,
                rate = 4.4,
                images = listOf(imageUrl(1007L)),
                rating = 4.4,
                characteristics = mapOf(
                    "Материал столешницы и сидений" to "тик",
                    "Материал каркаса" to "сталь с антикоррозийным покрытием",
                    "Цвет" to "натуральное дерево / чёрный",
                    "В комплекте" to "1 стол + 2 стула",
                    "Складной" to "да",
                    "Назначение" to "балкон, терраса, сад",
                ),
                stock = 15,
                deliveryInfo = "Доставка 2–4 дня",
                arMetadata = arMetadata("08_outdoor_set", 776f, 859f, 1831f),
            ),
        )

        if (productRepository.count() > 0) {
            return@CommandLineRunner
        }
        productRepository.saveAll(products)
    }
}
