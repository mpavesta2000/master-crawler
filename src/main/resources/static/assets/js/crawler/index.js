var swiper = new Swiper(".myCustomSwiper", {
    slidesPerView: 1,
    spaceBetween: 10,
    loop: true,
    rtl: true,
    pagination: {
        el: ".swiper-pagination",
        clickable: true,
    },
    navigation: {
        nextEl: ".myCustomSwiper .swiper-button-prev", // 🔄 Swapped
        prevEl: ".myCustomSwiper .swiper-button-next", // 🔄 Swapped
    },
    breakpoints: {
        576: { slidesPerView: 2 },
        768: { slidesPerView: 3 },
        992: { slidesPerView: 4 }
    }
});


var mybutton = document.getElementById("back-to-top");

window.onscroll = function () {
    scrollFunction();
};

function scrollFunction() {
    if (document.body.scrollTop > 100 || document.documentElement.scrollTop > 100) {
        mybutton.style.display = "block";
    } else {
        mybutton.style.display = "none";
    }
}

function topFunction() {
    document.body.scrollTop = 0;
    document.documentElement.scrollTop = 0;
}