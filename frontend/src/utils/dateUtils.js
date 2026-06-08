// Formate un timestamp en date lisible
export function formatDate(value) {
	if (!value) {
		return '--';
	}
	return new Date(value).toLocaleDateString('fr-FR', {
		day: '2-digit',
		month: '2-digit',
		year: 'numeric',
	});
}

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

// Retourne "il y a X min/h/j" par rapport à maintenant
export function timeAgo(value) {
	if (!value) {
		return '--';
	}
	const diff = (Date.now() - new Date(value).getTime()) / 1000;
	if (diff < 60) {
		return 'à l\'instant';
	} else if (diff < 3600) {
		return `il y a ${Math.floor(diff / 60)} min`;
	} else if (diff < 86400) {
		return `il y a ${Math.floor(diff / 3600)} h`;
	} else {
		return `il y a ${Math.floor(diff / 86400)} j`;
	}
}
