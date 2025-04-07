// Gestion des compétences et horaires
const skills = {
    init: () => {
        skills.render();
        skills.setupEventListeners();
    },

    render: () => {
        if (!profile.data) return;

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

        const hoursForm = document.getElementById('hoursForm');
        hoursForm.addEventListener('submit', skills.handleHoursSubmit);
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

            // Rafraîchir depuis le backend pour être sûr
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
    },

    handleHoursSubmit: async (event) => {
        event.preventDefault();

        const formData = {
            workingHoursWeekdays: document.getElementById('workingHoursWeekdays').value,
            workingHoursSaturday: document.getElementById('workingHoursSaturday').value,
            workingHoursSunday: document.getElementById('workingHoursSunday').value
        };

        try {
            const artisanId = localStorage.getItem('artisanId');
            await api.updateProfile(artisanId, formData);

            profile.data = { ...profile.data, ...formData };
            utils.showNotification('Horaires mis à jour');
        } catch (error) {
            utils.handleApiError(error);
        }
    }
};
