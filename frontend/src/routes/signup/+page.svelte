<script>
    import { goto } from '$app/navigation';

    let name = "";
    let emailInput = "";
    let pwInput = "";
    let isProcessing = false; //whether signup button is pressed

    async function handleSignup(){
        
        if (!name || !emailInput || !pwInput){
            alert("이름, 이메일, 비밀번호를 모두 입력해주세요.");
            return;
        }
        // const url = "http://52.78.16.144:8080/api/auth/signup";
        // const url = "/api/auth/signup";
        const url = "https://api.synclog.shop/api/auth/signup"

        isProcessing = true;

        try{
            const response = await fetch(url, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    name: name,
                    email: emailInput,
                    password: pwInput,
                }),
            });
        if (response.ok) {
            const result = await response.json();
            alert("회원가입 성공! 환영합니다.");
            console.log("서버 응답:", result);
            goto("/login");
        } else {
            const errorData = await response.json();
            console.error("회원가입 실패:", errorData);
            alert("가입 실패!" + (errorData.message || "다시 입력해주세요.")); 
            isProcessing = false;
        }
    } catch (error) {
        console.error("통신 에러:", error);
        alert("서버에 접속할 수 없습니다.");
    } finally {
        isProcessing = false;
    }
}
    function goToLogin(){
        goto("/login");
    }
</script>

<div class="container"> 
    <main class="card">
        <h1 class="title">Synclog</h1>
        <p class="subtitle">실시간 협업 문서 플랫폼</p>
            <div class="input-group">
                <label for="name">이름</label>
            <input 
            id="name"
            type="text"
            bind:value = {name}
            placeholder="홍길동"
            />
            </div>
            <div class="input-group">
                <label for="email">이메일</label>
            <input 
            id="email"
            type="email"
            bind:value = {emailInput}
            placeholder="example@email.com"
            />
            </div>
            <div class="input-group">
                <label for="password">비밀번호</label>
            <input 
            id = "password"
            type = "password" 
            bind:value = {pwInput}
            placeholder="********" 
            />
            {#if pwInput.length > 0 && pwInput.length < 8}
                <p class="error-msg">비밀번호는 8자 이상이어야 합니다.</p>
            {/if}
            </div>

        <button class="signup-btn" on:click={handleSignup} disabled = {isProcessing || pwInput.length < 8}>
            회원가입
        </button>
        <button class="login-link-btn" on:click={goToLogin}>이미 계정이 있으신가요? 로그인</button>
    </main>
</div>

<style>
    /* 전체 배경 및 중앙 정렬 */
    .container {
        display: flex;
        justify-content: center;
        align-items: center;
        min-height: 100vh;
        background-color: #f5f7ff; /* 은은한 하늘색 배경 */
    }

    /* 카드 박스 디자인 */
    .card {
        background: white;
        padding: 2.5rem;
        border-radius: 16px;
        box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05); /* 부드러운 그림자 */
        width: 100%;
        max-width: 400px; /* 카드의 최대 너비 설정 */
        text-align: center;
    }

    /* 제목 스타일 */
    .title {
        color: #4a3aff; /* 이미지의 보라색 포인트 컬러 */
        font-size: 1.5rem;
        font-weight: bold;
        margin-bottom: 0.5rem;
    }

    .subtitle {
        color: #666;
        font-size: 0.9rem;
        margin-bottom: 2rem;
    }

    /* 입력창 그룹 (라벨 + 인풋) 스타일 */
    .input-group {
        text-align: left;
        margin-bottom: 1.2rem;
    }

    label {
        display: block;
        font-size: 0.9rem;
        color: #333;
        margin-bottom: 0.5rem;
    }

    input {
        width: 100%;
        padding: 0.8rem;
        border: 1px solid #ddd;
        border-radius: 8px;
        font-size: 1rem;
        box-sizing: border-box; /* 패딩이 너비에 포함되도록 설정 */
        outline: none;
    }

    input:focus {
        border-color: #4a3aff; /* 포커스 시 보라색 테두리 */
    }

    .error-msg {
        color: red;
        font-size: 0.8rem;
        /* margin-top: -5px; */
        /* margin-bottom: 5px; */
    }

    /* 회원가입 버튼 스타일 */
    .signup-btn {
        width: 100%;
        padding: 0.9rem;
        background-color: #4a3aff;
        color: white;
        border: none;
        border-radius: 8px;
        font-size: 1rem;
        font-weight: bold;
        cursor: pointer;
        transition: background-color 0.2s;
        margin-top: 1rem;
        margin-bottom: 1rem;
    }

    .signup-btn:hover {
        background-color: #3a2add; /* 호버 시 조금 더 진하게 */
    }

    .signup-btn:disabled {
        background-color: #ccc;
        cursor: not-allowed;
    }

    /* 로그인 링크 버튼 스타일 */
    .login-link-btn {
        background: none;
        border: none;
        color: #4a3aff;
        font-size: 0.9rem;
        cursor: pointer;
    }
    
    .login-link-btn:hover {
        text-decoration: underline;
    }
</style>