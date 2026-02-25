package com.dsquares.library.data.network

import com.dsquares.library.data.network.model.items.Denomination
import com.dsquares.library.data.network.model.items.Item

object MockCouponData {

    // TODO: Replace with actual image URLs provided by user
    private const val IKEA_IMAGE = "https://play-lh.googleusercontent.com/5PCbqWUUvliDulNxGb42otjqQsAy8RIGpmmT3Ebe7v5_rxoUwYpHewF7wbbQD941MA"
    private const val NOON_IMAGE =
        "https://dummyimage.com/600x400/ffcc00/000000.png&text=NOON"

    private const val AMAZON_IMAGE =
        "https://dummyimage.com/600x400/232f3e/ffffff.png&text=AMAZON"

    private const val CARREFOUR_IMAGE =
        "https://dummyimage.com/600x400/003087/ffffff.png&text=CARREFOUR"
    private const val JARIR_IMAGE =
        "https://dummyimage.com/600x400/cc0000/ffffff.png&text=JARIR"

    private const val VIRGIN_IMAGE =
        "https://dummyimage.com/600x400/e60000/ffffff.png&text=VIRGIN"

    private const val NAMSHI_IMAGE =
        "https://dummyimage.com/600x400/000000/ffffff.png&text=NAMSHI"

    private const val TALABAT_IMAGE =
        "https://dummyimage.com/600x400/ff5a00/ffffff.png&text=TALABAT"

    private const val HM_IMAGE =
        "https://dummyimage.com/600x400/e50010/ffffff.png&text=H%26M"

    private const val NIKE_IMAGE =
        "https://dummyimage.com/600x400/000000/ffffff.png&text=NIKE"

    private const val ADIDAS_IMAGE =
        "https://dummyimage.com/600x400/000000/ffffff.png&text=ADIDAS"

    private const val SEPHORA_IMAGE =
        "https://dummyimage.com/600x400/000000/ffffff.png&text=SEPHORA"

    private const val LULU_IMAGE =
        "https://dummyimage.com/600x400/00a651/ffffff.png&text=LULU"

    val englishItems: List<Item> = listOf(
        // IKEA
        item("IKEA001", "IKEA Gift Card", IKEA_IMAGE, false, 500, "15", listOf("Shopping", "Home")),
        item("IKEA002", "IKEA Furniture Voucher", IKEA_IMAGE, false, 1200, "25", listOf("Shopping", "Home")),
        item("IKEA003", "IKEA Premium Card", IKEA_IMAGE, true, 3000, "40", listOf("Shopping", "Home")),
        // Noon
        item("NOON001", "Noon Shopping Voucher", NOON_IMAGE, false, 300, "10", listOf("Shopping")),
        item("NOON002", "Noon Electronics Deal", NOON_IMAGE, false, 800, "20", listOf("Shopping", "Electronics")),
        item("NOON003", "Noon VIP Coupon", NOON_IMAGE, true, 2000, "35", listOf("Shopping")),
        // Amazon
        item("AMZ001", "Amazon Gift Card", AMAZON_IMAGE, false, 400, "10", listOf("Shopping")),
        item("AMZ002", "Amazon Prime Voucher", AMAZON_IMAGE, false, 900, "20", listOf("Shopping", "Entertainment")),
        item("AMZ003", "Amazon Exclusive Deal", AMAZON_IMAGE, true, 2500, "30", listOf("Shopping")),
        // Carrefour
        item("CRF001", "Carrefour Grocery Voucher", CARREFOUR_IMAGE, false, 200, "5", listOf("Grocery")),
        item("CRF002", "Carrefour Weekly Deal", CARREFOUR_IMAGE, false, 450, "12", listOf("Grocery")),
        item("CRF003", "Carrefour Family Pack", CARREFOUR_IMAGE, false, 700, "18", listOf("Grocery")),
        // Starbucks
        item("JAR001", "Jarir Gift Card", JARIR_IMAGE, false, 350, "10", listOf("Shopping", "Electronics")),
        item("JAR002", "Jarir Tech Voucher", JARIR_IMAGE, false, 800, "20", listOf("Electronics")),
        item("JAR003", "Jarir Premium Card", JARIR_IMAGE, true, 1500, "25", listOf("Shopping", "Electronics")),
        // Virgin Megastore
        item("VRG001", "Virgin Music Card", VIRGIN_IMAGE, false, 300, "10", listOf("Entertainment")),
        item("VRG002", "Virgin Gaming Voucher", VIRGIN_IMAGE, false, 600, "15", listOf("Entertainment")),
        item("VRG003", "Virgin VIP Pass", VIRGIN_IMAGE, true, 1800, "30", listOf("Entertainment")),
        // Namshi
        item("NMS001", "Namshi Fashion Voucher", NAMSHI_IMAGE, false, 250, "10", listOf("Fashion")),
        item("NMS002", "Namshi Style Card", NAMSHI_IMAGE, false, 550, "20", listOf("Fashion")),
        item("NMS003", "Namshi Premium Deal", NAMSHI_IMAGE, true, 1200, "30", listOf("Fashion")),
        // Talabat
        item("TLB001", "Talabat Meal Voucher", TALABAT_IMAGE, false, 100, "10", listOf("Food & Drink")),
        item("TLB002", "Talabat Delivery Pass", TALABAT_IMAGE, false, 300, "15", listOf("Food & Drink")),
        item("TLB003", "Talabat Pro Membership", TALABAT_IMAGE, true, 800, "25", listOf("Food & Drink")),
        // H&M
        item("HM001", "H&M Fashion Card", HM_IMAGE, false, 200, "10", listOf("Fashion")),
        item("HM002", "H&M Season Pass", HM_IMAGE, false, 500, "20", listOf("Fashion")),
        item("HM003", "H&M VIP Card", HM_IMAGE, true, 1000, "30", listOf("Fashion")),
        // Nike
        item("NIKE001", "Nike Sports Voucher", NIKE_IMAGE, false, 400, "10", listOf("Fashion", "Sports")),
        item("NIKE002", "Nike Running Reward", NIKE_IMAGE, false, 750, "20", listOf("Fashion", "Sports")),
        item("NIKE003", "Nike Elite Card", NIKE_IMAGE, true, 2000, "35", listOf("Fashion", "Sports")),
        // Adidas
        item("ADI001", "Adidas Gift Card", ADIDAS_IMAGE, false, 350, "10", listOf("Fashion", "Sports")),
        item("ADI002", "Adidas Training Voucher", ADIDAS_IMAGE, false, 700, "20", listOf("Fashion", "Sports")),
        item("ADI003", "Adidas Premium Pass", ADIDAS_IMAGE, true, 1800, "30", listOf("Fashion", "Sports")),
        // Sephora
        item("SPH001", "Sephora Beauty Card", SEPHORA_IMAGE, false, 300, "10", listOf("Beauty")),
        item("SPH002", "Sephora Skincare Voucher", SEPHORA_IMAGE, false, 650, "20", listOf("Beauty")),
        item("SPH003", "Sephora VIP Reward", SEPHORA_IMAGE, true, 1500, "35", listOf("Beauty")),
        // Lulu Hypermarket
        item("LULU001", "Lulu Shopping Voucher", LULU_IMAGE, false, 150, "5", listOf("Grocery")),
        item("LULU002", "Lulu Weekly Deal", LULU_IMAGE, false, 400, "12", listOf("Grocery")),
        item("LULU003", "Lulu Family Card", LULU_IMAGE, false, 700, "20", listOf("Grocery")),
    )

    val arabicItems: List<Item> = listOf(
        // ايكيا
        item("IKEA001", "بطاقة هدية ايكيا", IKEA_IMAGE, false, 500, "15", listOf("تسوق", "منزل")),
        item("IKEA002", "قسيمة أثاث ايكيا", IKEA_IMAGE, false, 1200, "25", listOf("تسوق", "منزل")),
        item("IKEA003", "بطاقة ايكيا المميزة", IKEA_IMAGE, true, 3000, "40", listOf("تسوق", "منزل")),
        // نون
        item("NOON001", "قسيمة تسوق نون", NOON_IMAGE, false, 300, "10", listOf("تسوق")),
        item("NOON002", "عرض إلكترونيات نون", NOON_IMAGE, false, 800, "20", listOf("تسوق", "إلكترونيات")),
        item("NOON003", "كوبون نون VIP", NOON_IMAGE, true, 2000, "35", listOf("تسوق")),
        // أمازون
        item("AMZ001", "بطاقة هدية أمازون", AMAZON_IMAGE, false, 400, "10", listOf("تسوق")),
        item("AMZ002", "قسيمة أمازون برايم", AMAZON_IMAGE, false, 900, "20", listOf("تسوق", "ترفيه")),
        item("AMZ003", "عرض أمازون الحصري", AMAZON_IMAGE, true, 2500, "30", listOf("تسوق")),
        // كارفور
        item("CRF001", "قسيمة بقالة كارفور", CARREFOUR_IMAGE, false, 200, "5", listOf("بقالة")),
        item("CRF002", "عرض كارفور الأسبوعي", CARREFOUR_IMAGE, false, 450, "12", listOf("بقالة")),
        item("CRF003", "باقة كارفور العائلية", CARREFOUR_IMAGE, false, 700, "18", listOf("بقالة")),
        // مكتبة جرير
        item("JAR001", "بطاقة هدية جرير", JARIR_IMAGE, false, 350, "10", listOf("تسوق", "إلكترونيات")),
        item("JAR002", "قسيمة تقنية جرير", JARIR_IMAGE, false, 800, "20", listOf("إلكترونيات")),
        item("JAR003", "بطاقة جرير المميزة", JARIR_IMAGE, true, 1500, "25", listOf("تسوق", "إلكترونيات")),
        // فيرجن ميغاستور
        item("VRG001", "بطاقة موسيقى فيرجن", VIRGIN_IMAGE, false, 300, "10", listOf("ترفيه")),
        item("VRG002", "قسيمة ألعاب فيرجن", VIRGIN_IMAGE, false, 600, "15", listOf("ترفيه")),
        item("VRG003", "بطاقة فيرجن VIP", VIRGIN_IMAGE, true, 1800, "30", listOf("ترفيه")),
        // نمشي
        item("NMS001", "قسيمة أزياء نمشي", NAMSHI_IMAGE, false, 250, "10", listOf("أزياء")),
        item("NMS002", "بطاقة ستايل نمشي", NAMSHI_IMAGE, false, 550, "20", listOf("أزياء")),
        item("NMS003", "عرض نمشي المميز", NAMSHI_IMAGE, true, 1200, "30", listOf("أزياء")),
        // طلبات
        item("TLB001", "قسيمة وجبة طلبات", TALABAT_IMAGE, false, 100, "10", listOf("مأكولات ومشروبات")),
        item("TLB002", "بطاقة توصيل طلبات", TALABAT_IMAGE, false, 300, "15", listOf("مأكولات ومشروبات")),
        item("TLB003", "عضوية طلبات برو", TALABAT_IMAGE, true, 800, "25", listOf("مأكولات ومشروبات")),
        // اتش اند ام
        item("HM001", "بطاقة أزياء اتش اند ام", HM_IMAGE, false, 200, "10", listOf("أزياء")),
        item("HM002", "بطاقة موسم اتش اند ام", HM_IMAGE, false, 500, "20", listOf("أزياء")),
        item("HM003", "بطاقة VIP اتش اند ام", HM_IMAGE, true, 1000, "30", listOf("أزياء")),
        // نايكي
        item("NIKE001", "قسيمة نايكي الرياضية", NIKE_IMAGE, false, 400, "10", listOf("أزياء", "رياضة")),
        item("NIKE002", "مكافأة نايكي للجري", NIKE_IMAGE, false, 750, "20", listOf("أزياء", "رياضة")),
        item("NIKE003", "بطاقة نايكي إيليت", NIKE_IMAGE, true, 2000, "35", listOf("أزياء", "رياضة")),
        // أديداس
        item("ADI001", "بطاقة هدية أديداس", ADIDAS_IMAGE, false, 350, "10", listOf("أزياء", "رياضة")),
        item("ADI002", "قسيمة تدريب أديداس", ADIDAS_IMAGE, false, 700, "20", listOf("أزياء", "رياضة")),
        item("ADI003", "بطاقة أديداس المميزة", ADIDAS_IMAGE, true, 1800, "30", listOf("أزياء", "رياضة")),
        // سيفورا
        item("SPH001", "بطاقة جمال سيفورا", SEPHORA_IMAGE, false, 300, "10", listOf("جمال")),
        item("SPH002", "قسيمة عناية سيفورا", SEPHORA_IMAGE, false, 650, "20", listOf("جمال")),
        item("SPH003", "مكافأة سيفورا VIP", SEPHORA_IMAGE, true, 1500, "35", listOf("جمال")),
        // لولو هايبرماركت
        item("LULU001", "قسيمة تسوق لولو", LULU_IMAGE, false, 150, "5", listOf("بقالة")),
        item("LULU002", "عرض لولو الأسبوعي", LULU_IMAGE, false, 400, "12", listOf("بقالة")),
        item("LULU003", "بطاقة لولو العائلية", LULU_IMAGE, false, 700, "20", listOf("بقالة")),
    )

    private fun item(
        code: String,
        name: String,
        imageUrl: String,
        locked: Boolean,
        points: Int,
        discount: String,
        categories: List<String>
    ): Item = Item(
        code = code,
        name = name,
        imageUrl = imageUrl,
        locked = locked,
        rewardType = "gift_card",
        description = name,
        denominations = listOf(
            Denomination(
                brand = name.split(" ").first(),
                categories = categories,
                code = "$code-D1",
                denominationType = "fixed",
                description = null,
                discount = discount,
                from = null,
                imageUrl = null,
                inStock = true,
                name = name,
                points = points,
                redemptionChannel = null,
                redemptionFactor = null,
                termsAndConditions = null,
                to = null,
                usageInstructions = null,
                value = null
            )
        )
    )
}