
document.addEventListener("DOMContentLoaded", function () {
    let element = document.getElementById("persian-date");
    let newsPostedDate = element.getAttribute("data-date");

    if (newsPostedDate) {
    const postedDate = new Date(newsPostedDate);
    const formattedDate = new Intl.DateTimeFormat('fa-IR', {dateStyle: 'short', calendar: 'persian'}).format(postedDate);

    element.innerText = formattedDate;
}
});




document.addEventListener("DOMContentLoaded", function () {
    let elements = document.querySelectorAll("[data-date-comment]"); // Select all elements with data-date-comment

    elements.forEach(function (element) {
        let newsPostedDate = element.getAttribute("data-date-comment");

        if (newsPostedDate) {
            const postedDate = new Date(newsPostedDate);
            const formattedDate = new Intl.DateTimeFormat('fa-IR', {dateStyle: 'short', calendar: 'persian'}).format(postedDate);

            element.innerText = formattedDate; // Update the text for each element
        }
    });
});
