from flask import Flask, jsonify, render_template_string
import sqlite3

app = Flask(__name__)

HTML_TEMPLATE = """
<!DOCTYPE html>
<html>
<head>
    <title>스마트룸 모바일 모니터링 시스템</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        body { font-family: 'Arial', sans-serif; background-color: #f4f6f9; margin: 0; padding: 20px; text-align: center; }
        .container { max-width: 800px; margin: 0 auto; background: white; padding: 20px; border-radius: 15px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
        .status-box { padding: 15px; margin-bottom: 20px; border-radius: 10px; font-size: 1.2rem; font-weight: bold; color: white; transition: all 0.3s ease; }
        .peace { background-color: #2ecc71; }
        .danger { background-color: #e74c3c; animation: blink 1s infinite; }
        @keyframes blink { 0% { opacity: 1; } 50% { opacity: 0.7; } 100% { opacity: 1; } }
    </style>
</head>
<body>
    <div class="container">
        <h1>📱 모바일 시스템 스마트룸 대시보드</h1>
        <div id="statusCard" class="status-box peace">🏠 실시간 상태: 데이터 연결 중...</div>
        
        <div style="position: relative; height:40vh; width:100%">
            <canvas id="gyroChart"></canvas>
        </div>
    </div>

    <script>
        const ctx = document.getElementById('gyroChart').getContext('2d');
        const gyroChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: [],
                datasets: [
                    { label: 'X 축', data: [], borderColor: '#e74c3c', fill: false, tension: 0.1 },
                    { label: 'Y 축', data: [], borderColor: '#2ecc71', fill: false, tension: 0.1 },
                    { label: 'Z 축(보정)', data: [], borderColor: '#3498db', fill: false, tension: 0.1 }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: { y: { min: -15, max: 15 } }
            }
        });

        function fetchSensorData() {
            fetch(window.location.origin + '/api/data')
                .then(response => response.json())
                .then(data => {
                    if (data.length === 0) return;

                    const labels = [];
                    const xData = [];
                    const yData = [];
                    const zData = [];
                    let latestVibration = 0;

                    data.forEach(row => {
                        labels.push(row.time);
                        xData.push(row.x);
                        yData.push(row.y);
                        zData.push(row.z);
                        latestVibration = row.vibration;
                    });

                    gyroChart.data.labels = labels;
                    gyroChart.data.datasets[0].data = xData;
                    gyroChart.data.datasets[1].data = yData;
                    gyroChart.data.datasets[2].data = zData;
                    gyroChart.update();

                    const statusCard = document.getElementById('statusCard');
                    if (latestVibration === 1) {
                        statusCard.innerText = "🚨 [위험] 침입 및 외부 충격 감지!!";
                        statusCard.className = "status-box danger";
                    } else {
                        statusCard.innerText = "🏠 실시간 상태: 평화로움을 유지하는 중";
                        statusCard.className = "status-box peace";
                    }
                })
                .catch(err => console.error("데이터 패치 실패:", err));
        }

        setInterval(fetchSensorData, 500);
    </script>
</body>
</html>
"""

@app.route('/api/data')
def get_data():
    conn = sqlite3.connect('/home/pi/smartroom/smartroom.db')
    cursor = conn.cursor()
    cursor.execute("""
        SELECT x_axis, y_axis, z_axis, vibration_detected, strftime('%H:%M:%S', recorded_at) 
        FROM sensor_data 
        ORDER BY id DESC LIMIT 20
    """)
    rows = cursor.fetchall()
    conn.close()

    payload = []
    for r in reversed(rows):
        payload.append({
            'x': r[0],
            'y': r[1],
            'z': r[2],
            'vibration': r[3],
            'time': r[4]
        })
    return jsonify(payload)

@app.route('/')
def index():
    return render_template_string(HTML_TEMPLATE)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=False)