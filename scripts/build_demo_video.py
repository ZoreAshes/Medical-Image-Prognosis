"""
Build a LinkedIn-ready demo MP4 from captured UI frames.
Requires: pip install opencv-python-headless pillow numpy
"""

from __future__ import annotations

import sys
from pathlib import Path

import cv2
import numpy as np
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
FRAMES_DIR = ROOT / "demo-video" / "frames"
OUTPUT_MP4 = ROOT / "demo-video" / "xpert_linkedin_demo.mp4"

WIDTH = 1280
HEIGHT = 720
FPS = 30

SCENES = [
    {
        "type": "title",
        "duration": 4.0,
        "title": "Xpert",
        "subtitle": "Patient Management & Chest X-ray Analysis",
        "footer": "Java · JavaFX · MySQL · Python ML API",
    },
    {
        "type": "frame",
        "file": "01_login.png",
        "duration": 6.0,
        "caption": "Secure login and role-based access",
    },
    {
        "type": "frame",
        "file": "02_home_xray.png",
        "duration": 14.0,
        "caption": "Dashboard analytics with AI-assisted X-ray screening",
    },
    {
        "type": "frame",
        "file": "03_add_patient.png",
        "duration": 11.0,
        "caption": "Patient registration, records, and image upload",
    },
    {
        "type": "frame",
        "file": "04_patient_details.png",
        "duration": 11.0,
        "caption": "Prognosis tracking linked to each patient record",
    },
    {
        "type": "title",
        "duration": 4.0,
        "title": "Academic / portfolio project",
        "subtitle": "Not intended for clinical diagnosis",
        "footer": "Feedback welcome on LinkedIn",
    },
]


def load_font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    candidates = []
    if bold:
        candidates.extend(
            [
                "C:/Windows/Fonts/segoeuib.ttf",
                "C:/Windows/Fonts/arialbd.ttf",
            ]
        )
    else:
        candidates.extend(
            [
                "C:/Windows/Fonts/segoeui.ttf",
                "C:/Windows/Fonts/arial.ttf",
            ]
        )
    for path in candidates:
        if Path(path).exists():
            return ImageFont.truetype(path, size=size)
    return ImageFont.load_default()


def render_title_card(scene: dict) -> np.ndarray:
    img = Image.new("RGB", (WIDTH, HEIGHT), color=(32, 52, 76))
    draw = ImageDraw.Draw(img)

    title_font = load_font(72, bold=True)
    subtitle_font = load_font(34)
    footer_font = load_font(24)

    title = scene["title"]
    subtitle = scene["subtitle"]
    footer = scene["footer"]

    tw, th = draw.textbbox((0, 0), title, font=title_font)[2:]
    sw, sh = draw.textbbox((0, 0), subtitle, font=subtitle_font)[2:]
    fw, fh = draw.textbbox((0, 0), footer, font=footer_font)[2:]

    draw.text(((WIDTH - tw) / 2, HEIGHT * 0.34), title, fill=(255, 255, 255), font=title_font)
    draw.text(((WIDTH - sw) / 2, HEIGHT * 0.50), subtitle, fill=(190, 220, 210), font=subtitle_font)
    draw.text(((WIDTH - fw) / 2, HEIGHT * 0.72), footer, fill=(170, 190, 205), font=footer_font)

    return cv2.cvtColor(np.array(img), cv2.COLOR_RGB2BGR)


def render_frame_card(filename: str, caption: str) -> np.ndarray:
    path = FRAMES_DIR / filename
    if not path.exists():
        raise FileNotFoundError(f"Missing frame: {path}")

    screenshot = Image.open(path).convert("RGB")
    canvas = Image.new("RGB", (WIDTH, HEIGHT), color=(245, 247, 250))
    draw = ImageDraw.Draw(canvas)

    caption_font = load_font(28, bold=True)
    padding = 36
    caption_h = 56
    top = padding + caption_h

    draw.text((padding, padding), caption, fill=(35, 60, 82), font=caption_font)

    max_w = WIDTH - (padding * 2)
    max_h = HEIGHT - top - padding
    shot = screenshot.copy()
    shot.thumbnail((max_w, max_h), Image.Resampling.LANCZOS)

    x = (WIDTH - shot.width) // 2
    y = top + (max_h - shot.height) // 2
    canvas.paste(shot, (x, y))

    border = Image.new("RGB", (shot.width + 4, shot.height + 4), color=(210, 218, 228))
    canvas.paste(border, (x - 2, y - 2))
    canvas.paste(shot, (x, y))

    return cv2.cvtColor(np.array(canvas), cv2.COLOR_RGB2BGR)


def write_video() -> None:
    FRAMES_DIR.mkdir(parents=True, exist_ok=True)
    OUTPUT_MP4.parent.mkdir(parents=True, exist_ok=True)

    writer = cv2.VideoWriter(
        str(OUTPUT_MP4),
        cv2.VideoWriter_fourcc(*"mp4v"),
        FPS,
        (WIDTH, HEIGHT),
    )
    if not writer.isOpened():
        raise RuntimeError("Could not open video writer")

    for scene in SCENES:
        if scene["type"] == "title":
            image = render_title_card(scene)
        else:
            image = render_frame_card(scene["file"], scene["caption"])

        frame_count = int(scene["duration"] * FPS)
        for _ in range(frame_count):
            writer.write(image)

    writer.release()
    total_seconds = sum(scene["duration"] for scene in SCENES)
    print(f"Created {OUTPUT_MP4}")
    print(f"Duration: {total_seconds:.0f} seconds ({total_seconds / 60:.1f} min)")


if __name__ == "__main__":
    try:
        write_video()
    except Exception as exc:
        print(f"Error: {exc}", file=sys.stderr)
        sys.exit(1)
