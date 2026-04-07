<div align="center">
  <br/>
  <h1 style="border-bottom: none; margin-bottom: 0; color: #9D00FF; font-family: 'Helvetica Neue', sans-serif; font-size: 3em; font-weight: 800; letter-spacing: 2px;">
    ✦ EQUATION PLOTTER ✦
  </h1>
  <p style="font-family: 'Georgia', serif; font-style: italic; color: #666; font-size: 1.2em; margin-top: 10px;">
    An Elegant, High-Performance Mathematical Visualization Engine Built with JavaFX
  </p>

  <p align="center">
    <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white" alt="Java"/>
    <img src="https://img.shields.io/badge/JavaFX-000000?style=for-the-badge&logo=java&logoColor=white" alt="JavaFX"/>
    <img src="https://img.shields.io/badge/Exp4j-9D00FF?style=for-the-badge&logo=math&logoColor=white" alt="Exp4j"/>
    <img src="https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven"/>
  </p>
</div>

<br/>

<blockquote style="border-left: 4px solid #9D00FF; background-color: #f9f9f9; padding: 15px 20px; border-radius: 4px; font-family: 'Segoe UI', sans-serif; color: #444;">
  <strong>"Mathematics is not about numbers, equations, computations, or algorithms: it is about understanding."</strong><br>
  <em>— William Paul Thurston</em>
</blockquote>

<br/>

## ✧ About the Project

<div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333;">
  <p>
    <strong>Equation Plotter</strong> is a meticulously crafted, interactive graphical application designed to bring mathematics to life. Engineered for students, educators, and mathematics enthusiasts, it provides a seamless and visually stunning canvas for exploring complex mathematical relationships.
  </p>
  <p>
    Whether you are investigating the subtle curves of implicit equations, dissecting the topological depths of a 3D surface, or animating dynamic parametric loops, this tool handles the heavy computational lifting. It features an intelligent custom rendering engine—equipped with asymptotic circuit-breakers and marching-squares optimization—to ensure your mathematical exploration remains flawlessly smooth, visually accurate, and beautifully rendered.
  </p>
</div>

<hr style="border: 0; height: 1px; background-image: linear-gradient(to right, rgba(0, 0, 0, 0), rgba(157, 0, 255, 0.75), rgba(0, 0, 0, 0)); margin: 40px 0;">

## ✧ Mathematical Dimensions

<table width="100%" style="border: none;">
  <tr style="border: none;">
    <td width="33%" valign="top" style="border: none; padding: 15px; background-color: #fafafa; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">
      <h3 style="color: #9D00FF; margin-top: 0;">📐 2D Cartesian</h3>
      <p style="font-size: 0.9em; line-height: 1.5; color: #555;">
        The backbone of the plotting engine. Seamlessly parse and render both <strong>Explicit</strong> <code>y = f(x)</code> and <strong>Implicit</strong> <code>f(x,y) = 0</code> equations.<br><br>
        Powered by an advanced asymptotic detection algorithm, the 2D engine effortlessly maps discontinuities (like <code>tan(x)</code> or step functions) preventing jagged, false connections. Paired with infinite zoom and buttery-smooth panning.
      </p>
    </td>
    <td width="33%" valign="top" style="border: none; padding: 15px; background-color: #fafafa; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">
      <h3 style="color: #9D00FF; margin-top: 0;">🧊 3D Surfaces</h3>
      <p style="font-size: 0.9em; line-height: 1.5; color: #555;">
        Step beyond the flat plane. The 3D rendering module allows for the visualization of complex multivariable functions <code>z = f(x,y)</code>.<br><br>
        Features intuitive spatial camera controls—rotate, pan, and zoom through mathematical landscapes. Dynamic depth-shading and mesh rendering algorithms are utilized to vividly highlight topological intricacies, peaks, and valleys.
      </p>
    </td>
    <td width="33%" valign="top" style="border: none; padding: 15px; background-color: #fafafa; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">
      <h3 style="color: #9D00FF; margin-top: 0;">🌀 Polar & Parametric</h3>
      <p style="font-size: 0.9em; line-height: 1.5; color: #555;">
        Explore the beauty of rotational and time-based mathematics. Generate breathtaking rosettes, spirals, and limaçons by defining equations via <code>r = f(θ)</code> or <code>(x(t), y(t))</code>.<br><br>
        The engine seamlessly maps polar and parametric inputs onto the interactive canvas, allowing for flawless integration, layering, and comparison with standard Cartesian curves.
      </p>
    </td>
  </tr>
</table>

<br/>

## ✧ Elaborated Features

<div style="background-color: #ffffff; padding: 20px; border-left: 4px solid #333; box-shadow: 0 2px 8px rgba(0,0,0,0.08); border-radius: 4px;">

<h4 style="color: #222;">🎛️ Dynamic Parameter Animation</h4>
  <p style="color: #555;">Type an equation with undefined variables (e.g., <code>y = sin(a * x)</code>) and the engine automatically generates interactive UI sliders. Drag the slider to watch the graph morph in real-time, or press the <b>Play (▶)</b> button to let the application animate the parameter automatically.</p>

<h4 style="color: #222;">⚡ Fast-Render Marching Squares</h4>
  <p style="color: #555;">Heavy implicit equations (like <code>x^a + y^a = r^a</code>) traditionally lag user interfaces. Equation Plotter utilizes a highly optimized, double-buffered 1D array variation of the <i>Marching Squares</i> algorithm. When dragging sliders, the app drops into "Fast Render Mode", reducing grid density for 60fps real-time updates, snapping back to ultra-HD the moment you let go.</p>

<h4 style="color: #222;">🎯 Dynamic Point Tracking</h4>
  <p style="color: #555;">Define standalone coordinate points such as <code>(a, a^2)</code>. As you adjust the parameter <code>a</code>, the point dynamically traces along its mathematical path. Toggle the <b>Label (Aa)</b> button to display a real-time tracking tooltip of the point's exact current coordinates.</p>

<h4 style="color: #222;">🌙 Aesthetic UI & Dark Mode</h4>
  <p style="color: #555;">Engineered with JavaFX, the application features a floating, draggable equation panel with a modern glass-morphism aesthetic. Instantly toggle between a pristine Light Mode and a deep, immersive Dark Mode that dynamically recolors grids, axes, labels, and plot lines.</p>

</div>

<hr style="border: 0; height: 1px; background-image: linear-gradient(to right, rgba(0, 0, 0, 0), rgba(157, 0, 255, 0.75), rgba(0, 0, 0, 0)); margin: 40px 0;">