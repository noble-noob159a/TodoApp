#!/bin/bash
wait_for_user() {
    echo ""
    read -p "Nhấn Enter để thoát..."
}

echo "============================================"
echo "BẮT ĐẦU QUÁ TRÌNH BUILD & TEST SERS APP"
echo "============================================"

chmod +x gradlew

echo "Đang dọn dẹp (Clean)..."
./gradlew clean

if [ $? -ne 0 ]; then
    echo "Lỗi: Clean thất bại."
    exit 1
fi


echo "Đang tải dependencies và Compile..."
./gradlew assembleDebug

if [ $? -ne 0 ]; then
    echo "Lỗi: Compile thất bại. Hãy kiểm tra lại file build.gradle.kts"
    exit 1
fi

echo "Compile thành công! File .apk nằm tại: app/build/outputs/apk/debug/"


echo "Đang chạy Unit Tests..."
./gradlew testDebugUnitTest

if [ $? -ne 0 ]; then
    echo "Lỗi: Có Test Case không vượt qua (Failed)."
    echo "Xem chi tiết lỗi tại: app/build/reports/tests/testDebugUnitTest/index.html"
    exit 1
else
    echo "TẤT CẢ TEST ĐỀU PASS!"
fi

echo "============================================"
echo "HOÀN TẤT!"
echo "============================================"

wait_for_user