import { useEffect, useState, useRef } from 'react'

import "@olton/metroui/lib/metro.css";
import "@olton/metroui/lib/icons.css";
import "@olton/metroui/lib/metro.js";

function App() {
    // TODO : fetch user info or use context to get user info
    const User = {
        id: 1,
        firstname: "Mathis",
        lastname: "Delmaere",
        mail: "mathis.delmaere@etu.utc.fr"
    }

  return (
      <div>
        <header class="border border-size-1 bd-gray shadow-normal" data-role="appbar" data-expand-point="md">
            <ul class="app-bar-menu">
                <li><a>Accueil</a></li>
                <li><a>Planifier une discussion</a></li>
                <li><a>Mes salons de discussion</a></li>
                <li><a>Mes invitations</a></li>
            </ul>
            <div class="app-bar-item-static mx-auto">
                <p>{User.firstname} {User.lastname}</p>
            </div>

            <div class="app-bar-item-static ml-auto">
                <a>
                    <button class="small ml-1 alert">
                        Déconnexion
                    </button>
                </a>
            </div>
        </header>
      </div>
  );
}

export default App;
