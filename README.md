from flask import Flask, request, jsonify
import yt_dlp

app = Flask(__name__)

@app.route('/info')
def info():
    url = request.args.get('url')

    if not url:
        return jsonify({"error": "URL vazia"}), 400

    try:
        ydl_opts = {
            'quiet': True
        }

        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            info = ydl.extract_info(url, download=False)

            formats = []

            for f in info.get("formats", []):
                if f.get("ext") == "mp4" and f.get("height"):
                    formats.append({
                        "quality": f.get("height"),
                        "url": f.get("url")
                    })

            return jsonify({
                "title": info.get("title"),
                "thumbnail": info.get("thumbnail"),
                "formats": formats
            })

    except Exception as e:
        return jsonify({"error": str(e)}), 500


app.run(host="0.0.0.0", port=5000)
