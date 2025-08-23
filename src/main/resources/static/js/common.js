// ====== API 모듈 ======
const chatAPI = {
    // 인증 여부 확인
    isAuthenticated: () => {
        return !!localStorage.getItem("accessToken");
    },

    // 토큰 저장/조회
    setAccessToken: (token) => localStorage.setItem("accessToken", token),
    getAccessToken: () => localStorage.getItem("accessToken"),
    clearToken: () => localStorage.removeItem("accessToken"),

    // 공통 fetch
    async request(url, options = {}) {
        const headers = options.headers || {};
        headers["Authorization"] = `Bearer ${chatAPI.getAccessToken()}`;
        if (!(options.body instanceof FormData)) {
            headers["Content-Type"] = "application/json";
        }

        const response = await fetch(url, { ...options, headers });
        if (!response.ok) throw new Error("API 요청 실패");
        return await response.json();
    },

    // ==== Auth ====
    async logout() {
        try {
            await chatAPI.request("/api/v1/auth/logout", { method: "POST" });
        } finally {
            chatAPI.clearToken();
        }
    },

    // ==== User ====
    async getMyProfile() {
        return chatAPI.request("/api/v1/users/me");
    },
    async updateProfile(data, images) {
        const formData = new FormData();
        formData.append("data", new Blob([JSON.stringify(data)], { type: "application/json" }));
        if (images) {
            images.forEach((img) => formData.append("images", img));
        }
        return chatAPI.request("/api/v1/users/me", { method: "PUT", body: formData });
    },
    async registerUser(data, images) {
        const formData = new FormData();
        formData.append("data", new Blob([JSON.stringify(data)], { type: "application/json" }));
        if (images) {
            images.forEach((img) => formData.append("images", img));
        }
        return chatAPI.request("/api/v1/users/register", { method: "POST", body: formData });
    },
    async searchUsers(filters, page, size) {
        const params = new URLSearchParams({ ...filters, page, size });
        return chatAPI.request(`/api/v1/users?${params}`);
    },
    async getUserProfile(userId) {
        return chatAPI.request(`/api/v1/users/${userId}`);
    },

    // ==== ChatRoom ====
    async getChatRooms() {
        return chatAPI.request("/api/v1/chat-rooms");
    },
    async createOrFindChatRoom(userId) {
        return chatAPI.request(`/api/v1/chat-rooms`, {
            method: "POST",
            body: JSON.stringify({ opponentId: userId }),
        });
    },
    async exitChatRoom(roomId) {
        return chatAPI.request(`/api/v1/chat-rooms/${roomId}/exit`, { method: "POST" });
    },

    // ==== Message ====
    async getMessages(roomId, lastMessageId, size) {
        const params = new URLSearchParams();
        if (lastMessageId) params.append("lastMessageId", lastMessageId);
        if (size) params.append("size", size);
        return chatAPI.request(`/api/v1/chat-rooms/${roomId}/messages?${params}`);
    },
    async sendMessage(roomId, content) {
        return chatAPI.request(`/api/v1/chat-rooms/${roomId}/messages`, {
            method: "POST",
            body: JSON.stringify({ content }),
        });
    },
    async getLastReadMessageId(roomId) {
        return chatAPI.request(`/api/v1/chat-rooms/${roomId}/last-read`);
    },
    async markMessagesAsRead(roomId) {
        return chatAPI.request(`/api/v1/chat-rooms/${roomId}/read`, { method: "POST" });
    },

    // ==== Block ====
    async blockUser(userId) {
        return chatAPI.request(`/api/v1/blocks`, {
            method: "POST",
            body: JSON.stringify({ targetUserId: userId }),
        });
    },
};

// ====== UI Utils ======
const utils = {
    // 토스트 알림
    showToast(msg, type = "info") {
        const toast = document.createElement("div");
        toast.className = `toast ${type}`;
        toast.textContent = msg;
        document.body.appendChild(toast);
        setTimeout(() => {
            toast.classList.add("show");
            setTimeout(() => {
                toast.classList.remove("show");
                setTimeout(() => toast.remove(), 300);
            }, 2500);
        }, 50);
    },

    // Confirm 다이얼로그 (Promise 기반)
    confirm(message, okText = "확인", cancelText = "취소") {
        return new Promise((resolve) => {
            const overlay = document.createElement("div");
            overlay.className = "confirm-overlay";

            overlay.innerHTML = `
        <div class="confirm-box">
          <div class="confirm-message">${message.replace(/\n/g, "<br>")}</div>
          <div class="confirm-actions">
            <button class="confirm-cancel">${cancelText}</button>
            <button class="confirm-ok">${okText}</button>
          </div>
        </div>
      `;
            document.body.appendChild(overlay);

            overlay.querySelector(".confirm-cancel").onclick = () => {
                overlay.remove();
                resolve(false);
            };
            overlay.querySelector(".confirm-ok").onclick = () => {
                overlay.remove();
                resolve(true);
            };
        });
    },

    // 로딩 상태
    showLoading(container, message = "불러오는 중...") {
        container.innerHTML = `<div class="loading">${message}</div>`;
    },
    showError(container, message = "오류가 발생했습니다.") {
        container.innerHTML = `<div class="error">${message}</div>`;
    },
    showEmpty(container, icon, title, subtitle = "") {
        container.innerHTML = `
      <div class="empty-state">
        <div class="empty-icon">${icon}</div>
        <div class="empty-title">${title}</div>
        ${subtitle ? `<div class="empty-subtitle">${subtitle}</div>` : ""}
      </div>
    `;
    },

    // 지역 이름 변환
    getRegionName(code) {
        const map = {
            SEOUL: "서울",
            BUSAN: "부산",
            DAEGU: "대구",
            INCHEON: "인천",
            GWANGJU: "광주",
            DAEJEON: "대전",
            ULSAN: "울산",
            SEJONG: "세종",
            GYEONGGI: "경기",
            GANGWON: "강원",
            CHUNGBUK: "충북",
            CHUNGNAM: "충남",
            JEONBUK: "전북",
            JEONNAM: "전남",
            GYEONGBUK: "경북",
            GYEONGNAM: "경남",
            JEJU: "제주",
            UNKNOWN: "기타",
        };
        return map[code] || "기타";
    },

    // 시간/날짜 포맷
    formatTime(isoString) {
        if (!isoString) return "-";
        const d = new Date(isoString);
        return `${d.getHours().toString().padStart(2, "0")}:${d
            .getMinutes()
            .toString()
            .padStart(2, "0")}`;
    },
    formatMessageDate(isoString) {
        const d = new Date(isoString);
        return `${d.getFullYear()}.${d.getMonth() + 1}.${d.getDate()}`;
    },
    formatMessageTime(isoString) {
        const d = new Date(isoString);
        return `${d.getHours()}:${d.getMinutes().toString().padStart(2, "0")}`;
    },

    // 검색 필터 저장/로드
    saveSearchFilters(filters) {
        localStorage.setItem("searchFilters", JSON.stringify(filters));
    },
    loadSearchFilters() {
        try {
            return JSON.parse(localStorage.getItem("searchFilters")) || {};
        } catch {
            return {};
        }
    },
};
