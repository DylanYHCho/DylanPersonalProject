import time
import sqlite3
import Adafruit_ADXL345

accel = Adafruit_ADXL345.ADXL345()

conn = sqlite3.connect('/home/pi/smartroom/smartroom.db', isolation_level=None)
cursor = conn.cursor()

cursor.execute("""
    CREATE TABLE IF NOT EXISTS sensor_data (
        id INTEGER PRIMARY KEY AUTO_INCREMENT,
        x_axis REAL,
        y_axis REAL,
        z_axis REAL,
        vibration_detected INTEGER,
        recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )
""")

print("🚀 ADXL345 센서 수집 및 SQLite3 실시간 적재 시작...")

try:
    while True:
        x, y, z = accel.read()
        z_calibrated = z - 9.8 
        
        if abs(x) > 3.0 or abs(y) > 3.0 or abs(z_calibrated) > 3.0:
            vibration = 1
            print(f"🚨 [위험] 스마트룸 진동/움직임 감지! -> X: {x:.2f} | Y: {y:.2f} | Z(보정): {z_calibrated:.2f}")
        else:
            vibration = 0
            print(f"🏠 평화로운 상태 -> X: {x:.2f} | Y: {y:.2f} | Z(보정): {z_calibrated:.2f}")
            
        cursor.execute("""
            INSERT INTO sensor_data (x_axis, y_axis, z_axis, vibration_detected)
            VALUES (?, ?, ?, ?)
        """, (x, y, z_calibrated, vibration))
        
        time.sleep(1)

except KeyboardInterrupt:
    print("\n🛑 사용자에 의해 수집 프로그램이 안전하게 종료되었습니다.")
    conn.close()