import { useRef, useState } from 'react';
import { formatDuration } from '../utils/dateUtils';

// On créé un component spécifique pour les messages audio étant donné que 
// le component en lui-même embarque pas mal de style
// Note : ce component a été généré par IA
export default function VoiceMessageBubble({ src, isMe }) {
  const audioRef = useRef(null);
  const [playing, setPlaying] = useState(false);
  const [duration, setDuration] = useState(0);
  const [progress, setProgress] = useState(0);
  const [hasStarted, setHasStarted] = useState(false);

  const togglePlay = () => {
    const audio = audioRef.current;
    if (!audio) {
      return;
    }
    if (playing) {
      audio.pause();
    } else {
      audio.play();
    }
  };

  const barColor = isMe ? 'rgba(255,255,255,0.6)' : 'rgba(33,150,243,0.4)';
  const barColorActive = isMe ? '#fff' : '#2196F3';
  const playedBars = duration ? Math.round((progress / duration) * 24) : 0;
  const remaining = duration - progress;
  const displayTime = hasStarted ? remaining : duration;

  return (
    <div className="d-flex flex-align-center gap-2" style={{ minWidth: 180 }}>
      <button
        className="button square circle"
        onClick={togglePlay}
        style={{
          width: 32,
          height: 32,
          flexShrink: 0,
          backgroundColor: isMe ? 'rgba(255,255,255,0.2)' : '#e3f2fd',
          color: isMe ? '#fff' : '#2196F3',
          border: 'none',
        }}
      >
        <span className={playing ? 'mif-pause' : 'mif-play'} />
      </button>

      <div className="d-flex flex-align-center gap-1" style={{ flex: 1, height: 24 }}>
        {Array.from({ length: 24 }).map((_, i) => (
          <span
            key={i}
            style={{
              display: 'inline-block',
              width: 3,
              borderRadius: 2,
              height: 6 + (i % 5) * 4,
              backgroundColor: i < playedBars ? barColorActive : barColor,
            }}
          />
        ))}
      </div>

      <span style={{ fontSize: 11, flexShrink: 0 }}>
        {formatDuration(displayTime)}
      </span>

      <audio
        ref={audioRef}
        src={src}
        onPlay={() => { setPlaying(true); setHasStarted(true); }}
        onPause={() => setPlaying(false)}
        onEnded={() => { setPlaying(false); setHasStarted(false); setProgress(0); }}
        onLoadedMetadata={(e) => setDuration(e.target.duration)}
        onTimeUpdate={(e) => setProgress(e.target.currentTime)}
        style={{ display: 'none' }}
      />
    </div>
  );
}