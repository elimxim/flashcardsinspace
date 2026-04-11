window.loaderTimerId = setTimeout(() => {
    console.log("Loader timer expired")
    const loader = document.getElementById('app-spinner');
    if (loader) {
        loader.classList.add('visible');
        window.loaderAppearedAt = performance.now();
    }
}, 200);
