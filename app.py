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
            'quiet': True,
            'no_warnings': True
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

            # Obtém a thumbnail - sempre retorna a melhor qualidade
            thumbnail = info.get("thumbnail")

            # Se não encontrou a thumbnail padrão, tenta pegar da lista de thumbnails
            if not thumbnail and info.get("thumbnails"):
                thumbnails = info.get("thumbnails")
                # Pega a melhor qualidade (última da lista geralmente é a melhor)
                if thumbnails and len(thumbnails) > 0:
                    thumbnail = thumbnails[-1].get("url")

            print(f"Video: {info.get('title')}")
            print(f"Thumbnail: {thumbnail}")
            print(f"Formats encontrados: {len(formats)}")

            return jsonify({
                "title": info.get("title"),
                "thumbnail": thumbnail,
                "formats": formats
            })

    except Exception as e:
        print(f"Erro: {str(e)}")
        return jsonify({"error": str(e)}), 500


if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5000, debug=False)
