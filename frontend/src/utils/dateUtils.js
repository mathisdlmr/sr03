// Formate un timestamp en heure lisible
export function formatTime (value) {
	if (!value) {
		return '--';
	}
	return new Date(value).toLocaleTimeString('fr-FR', { 
		hour: '2-digit', 
		minute: '2-digit' 
	});
};

// Formate un timestamp en date + heure lisibles
export function formatDateTime(value) {
	if (!value) {
		return '--';
	}
	return new Date(value).toLocaleString('fr-FR', {
		day: '2-digit',
		month: '2-digit',
		year: 'numeric',
		hour: '2-digit',
		minute: '2-digit',
	});
}

// Formate une durée en secondes au format m:ss (pour les messages vocaux)
export function formatDuration(seconds) {
	if (!seconds || !Number.isFinite(seconds)) {
		return '0:00';
	}
	const totalSeconds = Math.floor(seconds);
	const minutes = Math.floor(totalSeconds / 60);
	const secs = totalSeconds % 60;
	return `${minutes}:${secs.toString().padStart(2, '0')}`;
}
