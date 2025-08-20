// 쿠키에서 값 꺼내기
function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
    return null;
}

// CSRF 토큰을 자동으로 헤더에 붙이는 fetch 래퍼
async function csrfFetch(url, options = {}) {
    const csrfToken = getCookie("XSRF-TOKEN");
    const headers = {
        ...(options.headers || {}),
        "X-CSRF-TOKEN": csrfToken
    };

    return fetch(url, {
        ...options,
        headers,
        credentials: "include" // 쿠키 포함 (accessToken, refreshToken 등)
    });
}

// 전역에서 사용 가능하게 export
window.csrfFetch = csrfFetch;
