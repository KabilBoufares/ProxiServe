// Gestion des compétences et horaires
const skills = {
    init: () => {
        skills.render();
        skills.setupEventListeners();
        hoursManager.init(); // 🔥 Ajouté ici pour activer la gestion des horaires
    },

    render: () => {
        if (!profile.data) return;

        // Rendu compétences
        const skillsList = document.getElementById('skillsList');
        skillsList.innerHTML = '';

        const skillsArray = Array.isArray(profile.data.skills) ? profile.data.skills : [];

        if (skillsArray.length === 0) {
            const emptyMsg = utils.createElement('p', {
                id: 'emptySkillsMessage',
                style: 'color: gray; font-style: italic;'
            }, ['Aucune compétence pour le moment.']);
            skillsList.appendChild(emptyMsg);
        } else {
            skillsArray.forEach(skill => {
                const skillElement = utils.createElement('div', { className: 'tag' }, [
                    document.createTextNode(skill),
                    utils.createElement('button', {
                        type: 'button',
                        'data-skill': skill
                    }, [
                        utils.createElement('i', { className: 'fas fa-times' })
                    ])
                ]);
                skillsList.appendChild(skillElement);
            });
        }

        // Horaires
        document.getElementById('workingHoursWeekdays').value = profile.data.workingHoursWeekdays || '';
        document.getElementById('workingHoursSaturday').value = profile.data.workingHoursSaturday || '';
        document.getElementById('workingHoursSunday').value = profile.data.workingHoursSunday || '';
    },

    setupEventListeners: () => {
        const addSkillBtn = document.getElementById('addSkillBtn');
        const newSkillInput = document.getElementById('newSkill');

        addSkillBtn.addEventListener('click', () => skills.handleAddSkill(newSkillInput.value));
        newSkillInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                skills.handleAddSkill(newSkillInput.value);
            }
        });

        document.getElementById('skillsList').addEventListener('click', (e) => {
            if (e.target.closest('button')) {
                const skill = e.target.closest('button').dataset.skill;
                skills.handleDeleteSkill(skill);
            }
        });
    },

    handleAddSkill: async (skill) => {
        skill = skill.trim();
        if (!skill) {
            utils.showNotification('Veuillez entrer une compétence', 'error');
            return;
        }

        try {
            const artisanId = localStorage.getItem('artisanId');
            const currentSkills = Array.isArray(profile.data.skills) ? profile.data.skills : [];
            if (currentSkills.includes(skill)) {
                utils.showNotification('Cette compétence existe déjà', 'warning');
                return;
            }

            const response = await fetch(`${API_URL}/artisans/${artisanId}/skills?skill=${encodeURIComponent(skill)}`, {
                method: 'PUT',
                headers: getHeaders()
            });

            if (!response.ok) throw new Error("Erreur lors de l'ajout de la compétence");

            profile.data = await api.getProfile(artisanId);
            skills.render();

            document.getElementById('newSkill').value = '';
            utils.showNotification('Compétence ajoutée avec succès');
        } catch (error) {
            utils.handleApiError(error);
        }
    },

    handleDeleteSkill: async (skill) => {
        try {
            const artisanId = localStorage.getItem('artisanId');
            await api.deleteSkill(artisanId, skill);

            profile.data.skills = profile.data.skills.filter(s => s !== skill);
            skills.render();
            utils.showNotification('Compétence supprimée');
        } catch (error) {
            utils.handleApiError(error);
        }
    }
};

// 🔧 Gestion des horaires en mode lecture seule avec édition contrôlée
const hoursManager = {
    original: {},

    init: () => {
        const editBtn = document.getElementById('editHoursBtn');
        const cancelBtn = document.getElementById('cancelHoursBtn');
        const form = document.getElementById('hoursForm');

        if (editBtn) {
            editBtn.addEventListener('click', () => hoursManager.toggleEditable(true));
        }

        if (cancelBtn) {
            cancelBtn.addEventListener('click', () => {
                hoursManager.restoreOriginal();
                hoursManager.toggleEditable(false);
            });
        }

        if (form) {
            form.addEventListener('submit', hoursManager.handleSubmit);
        }
    },

    toggleEditable: (editable) => {
        const fields = ['workingHoursWeekdays', 'workingHoursSaturday', 'workingHoursSunday'];
        fields.forEach(id => {
            const input = document.getElementById(id);
            if (editable) {
                hoursManager.original[id] = input.value;
                input.removeAttribute('readonly');
            } else {
                input.setAttribute('readonly', true);
            }
        });

        document.getElementById('editHoursControls').style.display = editable ? 'none' : 'block';
        document.getElementById('saveCancelHours').style.display = editable ? 'flex' : 'none';
    },

    restoreOriginal: () => {
        Object.entries(hoursManager.original).forEach(([id, value]) => {
            document.getElementById(id).value = value;
        });
    },

    handleSubmit: async (e) => {
        e.preventDefault();

        const formData = {
            workingHoursWeekdays: document.getElementById('workingHoursWeekdays').value.trim(),
            workingHoursSaturday: document.getElementById('workingHoursSaturday').value.trim(),
            workingHoursSunday: document.getElementById('workingHoursSunday').value.trim()
        };

        try {
            const artisanId = localStorage.getItem('artisanId');
            await api.updateProfile(artisanId, formData);

            profile.data = { ...profile.data, ...formData };
            utils.showNotification('Horaires mis à jour avec succès');
            hoursManager.toggleEditable(false);
        } catch (error) {
            utils.handleApiError(error);
        }
    }
};
