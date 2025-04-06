// Gestion des compétences et horaires
const skills = {
    // Initialisation
    init: () => {
        skills.render();
        skills.setupEventListeners();
    },

    // Affichage des compétences
    render: () => {
        if (!profile.data) return;

        // Affichage des compétences
        const skillsList = document.getElementById('skillsList');
        skillsList.innerHTML = '';

        profile.data.skills.forEach(skill => {
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

        // Affichage des horaires
        document.getElementById('workingHoursWeekdays').value = profile.data.workingHoursWeekdays;
        document.getElementById('workingHoursSaturday').value = profile.data.workingHoursSaturday;
        document.getElementById('workingHoursSunday').value = profile.data.workingHoursSunday;
    },

    // Configuration des écouteurs d'événements
    setupEventListeners: () => {
        // Ajout de compétence
        const addSkillBtn = document.getElementById('addSkillBtn');
        const newSkillInput = document.getElementById('newSkill');
        
        addSkillBtn.addEventListener('click', () => skills.handleAddSkill(newSkillInput.value));
        newSkillInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                skills.handleAddSkill(newSkillInput.value);
            }
        });

        // Suppression de compétence
        document.getElementById('skillsList').addEventListener('click', (e) => {
            if (e.target.closest('button')) {
                const skill = e.target.closest('button').dataset.skill;
                skills.handleDeleteSkill(skill);
            }
        });

        // Mise à jour des horaires
        const hoursForm = document.getElementById('hoursForm');
        hoursForm.addEventListener('submit', skills.handleHoursSubmit);
    },

    // Ajout d'une compétence
    handleAddSkill: async (skill) => {
        if (!skill.trim()) {
            utils.showNotification('Veuillez entrer une compétence', 'error');
            return;
        }

        try {
            const artisanId = localStorage.getItem('artisanId');
            const updatedProfile = await api.updateProfile(artisanId, {
                skills: [...profile.data.skills, skill]
            });

            profile.data.skills = updatedProfile.skills;
            skills.render();
            document.getElementById('newSkill').value = '';
            utils.showNotification('Compétence ajoutée');
        } catch (error) {
            utils.handleApiError(error);
        }
    },

    // Suppression d'une compétence
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

    // Mise à jour des horaires
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