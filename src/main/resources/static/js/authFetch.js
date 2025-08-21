// sessionStorage에 저장된 JWT 사용
function getAccessToken() {
    return sessionStorage.getItem("accessToken");
}

// fetch wrapper
async function authFetch(url, options = {}) {
    const token = sessionStorage.getItem("accessToken");
    const headers = {
        ...(options.headers || {}),
        "Content-Type": "application/json", // 기본값
    };

    if (token) {
        headers["Authorization"] = `Bearer ${token}`;
    }

    // body가 FormData면 Content-Type 헤더 제거
    if (options.body instanceof FormData) {
        delete headers["Content-Type"];
    }

    const res = await fetch(url, {
        ...options,
        headers,
        credentials: "include" // refresh 토큰 쿠키 때문에 필요
    });

    if (res.status === 401) {
        console.warn("Access token expired, try refresh flow");
        // TODO: refresh API 호출 후 재시도 로직 추가
    }

    return res;
}


window.addEventListener("load", () => {
    const hash = window.location.hash;
    if (hash.includes("accessToken=")) {
        const token = new URLSearchParams(hash.substring(1)).get("accessToken");
        sessionStorage.setItem("accessToken", token);
        history.replaceState(null, null, window.location.pathname);
    }
});

window.authFetch = authFetch;
